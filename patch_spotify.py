import re

with open('/root/SimpMusic/composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/ui/screen/player/content/NowPlayingContentSpotify.kt', 'r') as f:
    content = f.read()

# Remove the shadow modifier blocks
content = re.sub(r'\s*\.shadow\(\s*elevation = 3\.dp,\s*shape = RoundedCornerShape\(8\.dp\),\s*spotColor =.*?\n\s*\)', '', content)

# Change elevatedCardElevation(10.dp) to CardDefaults.cardElevation(defaultElevation = 0.dp)
content = re.sub(r'elevation = CardDefaults\.elevatedCardElevation\(10\.dp\)', 'elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)', content)

with open('/root/SimpMusic/composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/ui/screen/player/content/NowPlayingContentSpotify.kt', 'w') as f:
    f.write(content)
