package com.example.facefit.data.di

object AppModule { // hilt injection
    // 1. Add Hilt dependencies in build.gradle (app level)
    // 2. Create a class annotated with @Module and @InstallIn(SingletonComponent::class)
    // 3. Provide dependencies using @Provides or @Binds annotations
    // 4. Use @Inject constructor in classes where you want to inject dependencies
    // 5. Annotate your Application class with @HiltAndroidApp

}