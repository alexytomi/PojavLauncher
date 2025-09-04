#include <jni.h>
#include <android/log.h>
#include <bytehook.h>
#include <log.h>
#include <string.h>

static JavaVM *g_vm = NULL;
static jclass mainActivity = NULL;
static int isInitialized = 0; // Yes this is very wrong but it works, I don't wanna possibly grind
                              // the dlopens to a potential halt.
//static jclass g_main_activity_cls = NULL;
//static jmethodID g_setupSDL_mid = NULL;

static void pre_dlopen_callback(const char *filename, void *data) {
    __android_log_print(ANDROID_LOG_INFO, TAG,
                        "Library is about to be loaded: %s", filename);
}

static void post_dlopen_callback(const char *filename, int result, void *data) {
    __android_log_print(ANDROID_LOG_INFO, TAG,
                        "Library %s was loaded, result: %d", filename, result);
    if (filename) {
        const char *base = strrchr(filename, '/');
        if (!base) base = filename; else base++;
        if (strcmp(base, "libSDL3.so") == 0) {
            if (isInitialized) return;
            JNIEnv *env = NULL;
            if ((*g_vm)->GetEnv(g_vm, (void**)&env, JNI_VERSION_1_6) != JNI_OK) {
                if ((*g_vm)->AttachCurrentThread(g_vm, &env, NULL) != JNI_OK) {
                    __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to attach thread");
                    return;
                }
            }
            isInitialized++;
            jmethodID setupMID = (*env)->GetStaticMethodID(env, mainActivity, "setupSDL", "()V");
            (*env)->CallStaticVoidMethod(env, mainActivity, setupMID);
            (*g_vm)->DetachCurrentThread(g_vm);
            LOGI("SDL: Initialized Java JNI!");
        }
    }
}

void register_dlopen_callbacks(JNIEnv *env, jclass clazz) {
    (*env)->GetJavaVM(env, &g_vm);
    bytehook_add_dlopen_callback(
            pre_dlopen_callback,
            post_dlopen_callback,
            NULL
    );
}

JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_MainActivity_registerSDLCallback(JNIEnv *env, jclass clazz, jclass MainActivityClass) {
    mainActivity = (*env)->NewGlobalRef(env, MainActivityClass);

}