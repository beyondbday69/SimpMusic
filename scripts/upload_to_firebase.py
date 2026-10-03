#!/usr/bin/env python3
"""
Uploads an Android APK to Firebase App Distribution via Google's REST API.
Works across all CI runners and local environments without requiring Docker or external plugins.
"""

import argparse
import base64
import json
import os
import sys
import time
import urllib.parse
import urllib.request
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives.serialization import load_pem_private_key


def get_oauth2_token(sa_data: dict) -> str:
    now = int(time.time())
    header = {'alg': 'RS256', 'typ': 'JWT'}
    payload = {
        'iss': sa_data['client_email'],
        'sub': sa_data['client_email'],
        'aud': sa_data.get('token_uri', 'https://oauth2.googleapis.com/token'),
        'iat': now,
        'exp': now + 3600,
        'scope': 'https://www.googleapis.com/auth/cloud-platform',
    }

    def b64url(b: bytes) -> str:
        return base64.urlsafe_b64encode(b).decode('utf-8').rstrip('=')

    h_b64 = b64url(json.dumps(header).encode('utf-8'))
    p_b64 = b64url(json.dumps(payload).encode('utf-8'))
    signing_input = f'{h_b64}.{p_b64}'.encode('utf-8')

    key = load_pem_private_key(sa_data['private_key'].encode('utf-8'), password=None)
    sig = key.sign(signing_input, padding.PKCS1v15(), hashes.SHA256())
    jwt = f'{h_b64}.{p_b64}.{b64url(sig)}'

    token_url = sa_data.get('token_uri', 'https://oauth2.googleapis.com/token')
    data = urllib.parse.urlencode({
        'grant_type': 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        'assertion': jwt,
    }).encode('utf-8')

    req = urllib.request.Request(token_url, data=data, method='POST')
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode('utf-8'))['access_token']


def upload_apk(
    apk_path: str,
    sa_data: dict,
    app_id: str,
    release_notes: str = '',
    groups: str = '',
):
    if not os.path.exists(apk_path):
        print(f"❌ APK file not found at: {apk_path}", file=sys.stderr)
        sys.exit(1)

    apk_size = os.path.getsize(apk_path)
    file_name = os.path.basename(apk_path)
    print(f"📦 Uploading {file_name} ({apk_size / (1024 * 1024):.1f} MB)...")

    token = get_oauth2_token(sa_data)
    proj_num = app_id.split(':')[1] if ':' in app_id else sa_data.get('project_id', '')

    # 1. Initiate resumable upload
    init_url = (
        f"https://firebaseappdistribution.googleapis.com/upload/v1/"
        f"projects/{proj_num}/apps/{app_id}/releases:upload"
    )
    headers = {
        'Authorization': f'Bearer {token}',
        'X-Goog-Upload-File-Name': file_name,
        'X-Goog-Upload-Protocol': 'resumable',
        'X-Goog-Upload-Command': 'start',
        'Content-Type': 'application/octet-stream',
    }

    req_init = urllib.request.Request(init_url, headers=headers, method='POST')
    try:
        with urllib.request.urlopen(req_init) as resp_init:
            upload_url = resp_init.headers.get('X-Goog-Upload-URL')
    except urllib.error.HTTPError as e:
        print(f"❌ Failed to initiate upload: {e.code} - {e.read().decode('utf-8')}", file=sys.stderr)
        sys.exit(1)

    if not upload_url:
        print("❌ Did not receive upload URL from Firebase.", file=sys.stderr)
        sys.exit(1)

    # 2. Upload file content
    with open(apk_path, 'rb') as f:
        apk_data = f.read()

    upload_headers = {
        'Authorization': f'Bearer {token}',
        'X-Goog-Upload-Command': 'upload, finalize',
        'X-Goog-Upload-Offset': '0',
        'Content-Type': 'application/octet-stream',
        'Content-Length': str(len(apk_data)),
    }

    req_upload = urllib.request.Request(upload_url, data=apk_data, headers=upload_headers, method='POST')
    try:
        with urllib.request.urlopen(req_upload) as resp_upload:
            res = json.loads(resp_upload.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        print(f"❌ Upload failed: {e.code} - {e.read().decode('utf-8')}", file=sys.stderr)
        sys.exit(1)

    # 3. Poll operation until done
    op_name = res.get('name')
    release_info = None

    if op_name and not res.get('done'):
        print("⏳ Processing APK in Firebase App Distribution...")
        op_url = f"https://firebaseappdistribution.googleapis.com/v1/{op_name}"
        for _ in range(60):
            time.sleep(3)
            op_req = urllib.request.Request(op_url, headers={'Authorization': f'Bearer {token}'})
            try:
                with urllib.request.urlopen(op_req) as op_resp:
                    op_data = json.loads(op_resp.read().decode('utf-8'))
                    if op_data.get('done'):
                        release_info = op_data.get('response', {}).get('release')
                        break
            except Exception as e:
                print(f"Polling error: {e}")
    else:
        release_info = res.get('release')

    if not release_info:
        print("❌ Could not get release details from Firebase response.", file=sys.stderr)
        sys.exit(1)

    release_name = release_info.get('name')
    display_version = release_info.get('displayVersion')
    build_version = release_info.get('buildVersion')
    console_uri = release_info.get('firebaseConsoleUri')
    testing_uri = release_info.get('testingUri')

    print("\n✅ Firebase App Distribution Release Created!")
    print(f"   • Version: {display_version} ({build_version})")
    print(f"   • Console: {console_uri}")
    print(f"   • Testing App: {testing_uri}")

    # 4. Optional: add release notes
    if release_notes and release_name:
        notes_url = f"https://firebaseappdistribution.googleapis.com/v1/{release_name}?updateMask=release_notes.text"
        notes_body = json.dumps({'releaseNotes': {'text': release_notes}}).encode('utf-8')
        notes_req = urllib.request.Request(
            notes_url,
            data=notes_body,
            headers={
                'Authorization': f'Bearer {token}',
                'Content-Type': 'application/json',
            },
            method='PATCH',
        )
        try:
            with urllib.request.urlopen(notes_req):
                print("   • Release notes updated successfully.")
        except Exception as e:
            print(f"   ⚠️ Could not update release notes: {e}")

    # 5. Optional: distribute to tester groups
    if groups and release_name:
        group_list = [g.strip() for g in groups.split(',') if g.strip()]
        dist_url = f"https://firebaseappdistribution.googleapis.com/v1/{release_name}:distribute"
        dist_body = json.dumps({'groupAliases': group_list}).encode('utf-8')
        dist_req = urllib.request.Request(
            dist_url,
            data=dist_body,
            headers={
                'Authorization': f'Bearer {token}',
                'Content-Type': 'application/json',
            },
            method='POST',
        )
        try:
            with urllib.request.urlopen(dist_req):
                print(f"   • Distributed to groups: {', '.join(group_list)}")
        except Exception as e:
            print(f"   ℹ️ Group distribution notice: {e}")


def main():
    parser = argparse.ArgumentParser(description="Upload APK to Firebase App Distribution")
    parser.add_argument("--apk", required=True, help="Path to APK file")
    parser.add_argument("--app-id", default="1:160945537270:android:0a1116bfe83ad9e169ecd7", help="Firebase App ID")
    parser.add_argument("--service-account", help="Service Account JSON string or file path")
    parser.add_argument("--release-notes", default="", help="Release notes text")
    parser.add_argument("--groups", default="", help="Comma-separated tester group aliases")
    args = parser.parse_args()

    sa_input = args.service_account or os.environ.get("FIREBASE_SERVICE_ACCOUNT")
    if not sa_input:
        print("❌ Error: Missing Firebase service account credentials. Provide via --service-account or FIREBASE_SERVICE_ACCOUNT env var.", file=sys.stderr)
        sys.exit(1)

    if os.path.isfile(sa_input):
        with open(sa_input, 'r') as f:
            sa_data = json.load(f)
    else:
        try:
            sa_data = json.loads(sa_input)
        except json.JSONDecodeError as e:
            print(f"❌ Failed to parse service account JSON: {e}", file=sys.stderr)
            sys.exit(1)

    upload_apk(
        apk_path=args.apk,
        sa_data=sa_data,
        app_id=args.app_id,
        release_notes=args.release_notes,
        groups=args.groups,
    )


if __name__ == '__main__':
    main()
