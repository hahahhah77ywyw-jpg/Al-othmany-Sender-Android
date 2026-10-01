# بناء APK من GitHub — Al-othmany Sender 4.1.0

1. أنشئ مستودعًا جديدًا على GitHub.
2. ارفع **كل محتويات هذا المجلد** إلى جذر المستودع، بحيث يظهر `settings.gradle` في الصفحة الرئيسية.
3. ارفع الملفات ثم افتح تبويب **Actions**.
4. اختر **Build Al-othmany Sender APK**.
5. اضغط **Run workflow**.
6. بعد انتهاء البناء افتح نتيجة التشغيل وانزل إلى **Artifacts**.
7. نزّل `Al-othmany-Sender-4.1.0-debug` وفك الضغط للحصول على `app-debug.apk`.
8. ثبّت APK على الهاتف.

## ملاحظة
هذا Workflow يبني نسخة Debug. لا يحتاج Android Studio ولا Gradle مثبتًا على جهازك؛ GitHub يوفر بيئة البناء.

بعد التثبيت، إذا أردت استخدام التحكم داخل WhatsApp، فعّل خدمة Accessibility الخاصة بـ Al-othmany Sender من إعدادات إمكانية الوصول في Android.
