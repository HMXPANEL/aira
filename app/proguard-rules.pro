# Keep Gemini SDK models
-keep class com.google.ai.client.generativeai.** { *; }

# Keep Room entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
