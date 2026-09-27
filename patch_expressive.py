with open('/root/SimpMusic/composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/ui/screen/player/content/NowPlayingExpressiveCards.kt', 'r') as f:
    content = f.read()

content = content.replace('elevation = CardDefaults.elevatedCardElevation(10.dp)', 'elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)')

with open('/root/SimpMusic/composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/ui/screen/player/content/NowPlayingExpressiveCards.kt', 'w') as f:
    f.write(content)
