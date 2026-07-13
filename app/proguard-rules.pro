# Project R8 / ProGuard keep rules for the release (minified) build.
#
# Most libraries here ship their own *consumer* rules (Room, Play Ads/UMP, Firebase, Koin),
# so this file focuses on the one thing R8 can't infer on its own: kotlinx.serialization, which
# backs both the bundled question-bank JSON models and the type-safe @Serializable nav routes.

# --- kotlinx.serialization -------------------------------------------------------------
# Canonical rules from the kotlinx.serialization project. Keep generated $serializer classes,
# Companion serializer() accessors, and the annotations R8 needs to resolve them.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,Signature,InnerClasses,EnclosingMethod

-keepclassmembers class **$$serializer { *; }

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$Companion Companion;
}
-keepclassmembers class <2>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Serializable objects (e.g. object nav routes): keep the INSTANCE + serializer().
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-dontnote kotlinx.serialization.**

# --- Enums used by name via valueOf(...) -----------------------------------------------
# Domain/QuizType/QuizStatus round-trip through Enum.valueOf when rehydrating persisted rows
# and decoding config JSON. Keep enum synthetic members so name-based lookup survives shrinking.
-keepclassmembers enum com.fax.passyourpmpexam.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
