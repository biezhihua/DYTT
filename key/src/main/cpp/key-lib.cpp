#include <jni.h>
#include <cstdlib>
#include <cstring>
#include "MD5.h"

const char *ENCRYPT_KEY = "wq0LQbLUTH66";
const char *X_HEADER_REQUEST_TIMESTAMP = "x-header-request-timestamp";
const char *X_HEADER_REQUEST_IMEI = "x-header-request-imei";
const char *RELEASE_SIGN = "308202c7308201afa003020102020452b2486a300d06092a864886f70d01010b0500301431123010060355040313096269657a6869687561301e170d3136303432333039323234315a170d3431303431373039323234315a301431123010060355040313096269657a686968756130820122300d06092a864886f70d01010105000382010f003082010a028201010085c60080d74faae2d170bc58294a43bec63d793432352346bd2807eb86dc0f3887e16d9236cba22d6c2058b682312eb52fc63477a290022f650e391bcf6599e1b982f1ab8467a9d54f34d95926ff76ab75f6b9c5452e6148d8225b017223964915c639179e893e654284abc7b91bab106a5ebd4827008d9b73438bbd8fd43767c76b84282040fad407877de9ef324d26c725dbea124fd73e8716fab027cbe483c9876348af4fc2505b5595c50c4ccf2d044b6bca2eb2b421aa5e65d2970bffa40586f2912f17dbd8a2300c7a376a4586720580306909b2925317faba2719ab50af05136462e63d4969bf17522b5670f89525fabb3708b01d785e505c03b766c10203010001a321301f301d0603551d0e04160414af974d5f2065256795cdfccbe59f056597413832300d06092a864886f70d01010b0500038201010030816fbd00d70eea9dc74a2051c7a2cb1e2a8308ae16a440295b10a964d3dc92b3d3c87bc1f8fa19d253c9dfb6305e7443325ad2e4fd698f7304864dd66a675f8c0b6a7e93a046c59e90dd3a632b8390338808fd969655112159646d60779191a15cf1fa3ab135e09a77461c1e774d8206dc29c933387a9aa2130a457a287aae72a84cc9e0f34d775a1b620f29fa0503b8b044f24c2ac9230fdc2fee17c8882a88d3adf07419b8811cc8a4bb94fba36759c655a6016d2abaccd8038801a75df8053fa5d5689de6836a13ecd526c8d04778f69182977bc01f28e89d3220a7d16bd19d03cccdab9680420fa6ca1bc8e1cf38c62ad75cb482fb722f9be2478fb218";
const char *DEBUG_SIGN = "308201dd30820146020101300d06092a864886f70d010105050030373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b3009060355040613025553301e170d3138303431333136323435365a170d3438303430353136323435365a30373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b300906035504061302555330819f300d06092a864886f70d010101050003818d0030818902818100b0f2b2038e0ea2427e65c37f1d02bd973968e11eba1d6c5992d5ffa0fa4c02aaa0a7ee2098d4e6f80db7344551b3865f1724e34ce0a0a0c2582b0f34d8405ae37c250b114f7333efce73cff14bf7951687773012617041771fba27ede83154f95325830e4397e871ef0bba97658a02893d14d470f8e7d2742dee8d88d013387f0203010001300d06092a864886f70d0101050500038181006322e20a916bae0247930bfe987261a60e3c5af892e7167b067b0770f52494338d490ea576041d0e8d476e4cc5eccfb121bc1457ab373846dc830f066c1ce8501851d20ee8cb8f1c0690a0c2baeaa804bfb268c3290b4508ba84fd94644f6d27889c9d59b5e6b8319206f28f47958485998e239b1ce306deb7b48077f03650ef";

extern "C"
JNIEXPORT jstring JNICALL
Java_com_bzh_dytt_key_KeyUtils_getKey__Ljava_lang_String_2Ljava_lang_String_2(JNIEnv *env,
                                                                              jclass type,
                                                                              jstring currentTime_,
                                                                              jstring imei_) {

    const char *currentTime = env->GetStringUTFChars(currentTime_, 0);
    const char *imei = env->GetStringUTFChars(imei_, 0);

    char *dest = (char *) malloc(120);
    strcpy(dest, ENCRYPT_KEY);
    strcat(dest, X_HEADER_REQUEST_TIMESTAMP);
    strcat(dest, "=");
    strcat(dest, currentTime);
    strcat(dest, X_HEADER_REQUEST_IMEI);
    strcat(dest, "=");
    strcat(dest, imei);

    env->ReleaseStringUTFChars(currentTime_, currentTime);
    env->ReleaseStringUTFChars(imei_, imei);

    return env->NewStringUTF(dest);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_bzh_dytt_key_KeyUtils_getKey__Landroid_content_Context_2Ljava_lang_String_2Ljava_lang_String_2(
        JNIEnv *env, jclass type, jobject context, jstring currentTime_, jstring imei_) {


    jclass native_class = env->GetObjectClass(context);
    jmethodID pm_id = env->GetMethodID(native_class, "getPackageManager",
                                       "()Landroid/content/pm/PackageManager;");
    jobject pm_obj = env->CallObjectMethod(context, pm_id);
    jclass pm_clazz = env->GetObjectClass(pm_obj);

    // 得到 getPackageInfo 方法的 ID
    jmethodID package_info_id = env->GetMethodID(pm_clazz, "getPackageInfo",
                                                 "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jclass native_classs = env->GetObjectClass(context);
    jmethodID mId = env->GetMethodID(native_classs, "getPackageName", "()Ljava/lang/String;");
    jstring pkg_str = static_cast<jstring>(env->CallObjectMethod(context, mId));
    // 获得应用包的信息
    jobject pi_obj = env->CallObjectMethod(pm_obj, package_info_id, pkg_str, 64);
    // 获得 PackageInfo 类
    jclass pi_clazz = env->GetObjectClass(pi_obj);
    // 获得签名数组属性的 ID
    jfieldID signatures_fieldId = env->GetFieldID(pi_clazz, "signatures",
                                                  "[Landroid/content/pm/Signature;");
    jobject signatures_obj = env->GetObjectField(pi_obj, signatures_fieldId);
    jobjectArray signaturesArray = (jobjectArray) signatures_obj;
    jsize size = env->GetArrayLength(signaturesArray);
    jobject signature_obj = env->GetObjectArrayElement(signaturesArray, 0);
    jclass signature_clazz = env->GetObjectClass(signature_obj);
    jmethodID string_id = env->GetMethodID(signature_clazz, "toCharsString",
                                           "()Ljava/lang/String;");
    jstring str = static_cast<jstring>(env->CallObjectMethod(signature_obj, string_id));
    char *c_msg = (char *) env->GetStringUTFChars(str, 0);

    if (strcmp(c_msg, RELEASE_SIGN) == 0 ||
        strcmp(c_msg, DEBUG_SIGN) == 0 ||
        true) {

        const char *currentTime = env->GetStringUTFChars(currentTime_, 0);
        const char *imei = env->GetStringUTFChars(imei_, 0);

        char *dest = (char *) malloc(120);
        strcpy(dest, ENCRYPT_KEY);
        strcat(dest, X_HEADER_REQUEST_TIMESTAMP);
        strcat(dest, "=");
        strcat(dest, currentTime);
        strcat(dest, X_HEADER_REQUEST_IMEI);
        strcat(dest, "=");
        strcat(dest, imei);

        env->ReleaseStringUTFChars(currentTime_, currentTime);
        env->ReleaseStringUTFChars(imei_, imei);

        MD5 md5 = MD5(dest);
        std::string md5Result = md5.hexdigest();
        return env->NewStringUTF(md5Result.c_str());
    } else {
        return (env)->NewStringUTF("63daeccb2fd073c8c79c342ef21cb6e8");
    }
}