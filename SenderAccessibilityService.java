private void pollInvite(String pkg, String message, Result cb,
                        long deadline, boolean clicked) {

    if (System.currentTimeMillis() > deadline) {
        finish(cb, false, "لم يتم تأكيد الانضمام داخل المهلة");
        return;
    }

    if (!pkg.equals(getRootPackage())) {
        final boolean nextClicked = clicked;
        h.postDelayed(() ->
                pollInvite(pkg, message, cb, deadline, nextClicked), 350);
        return;
    }

    AccessibilityNodeInfo root = getRootInActiveWindow();

    if (root == null) {
        final boolean nextClicked = clicked;
        h.postDelayed(() ->
                pollInvite(pkg, message, cb, deadline, nextClicked), 350);
        return;
    }

    boolean clickedNow = clicked;

    if (!clickedNow) {
        AccessibilityNodeInfo join = find(
                root,
                "انضمام إلى المجموعة",
                "انضمام",
                "Join group",
                "Join"
        );

        if (join != null) {
            if (click(join)) {
                clickedNow = true;
            }
        } else if (hasComposer(root)) {
            clickedNow = true;
        }
    }

    if (clickedNow && hasComposer(root)) {

        if (message != null && !message.trim().isEmpty()) {

            boolean sent = sendMessage(root, message);

            if (!sent) {
                final boolean nextClicked = true;
                h.postDelayed(() ->
                        pollInvite(pkg, message, cb, deadline, nextClicked), 400);
                return;
            }

            h.postDelayed(() ->
                    finish(cb, true,
                            "تم الانضمام/الدخول وإرسال الرسالة"), 650);
            return;
        }

        finish(cb, true, "تم الانضمام/الدخول إلى المجموعة");
        return;
    }

    // Existing membership can land directly in a group chat
    // without the join button.
    if (!clickedNow && hasComposer(root)) {

        if (message == null || message.trim().isEmpty()) {
            finish(cb, true, "المجموعة مفتوحة بالفعل");
            return;
        }

        if (sendMessage(root, message)) {
            h.postDelayed(() ->
                    finish(cb, true,
                            "المجموعة المفتوحة وتم إرسال الرسالة"), 650);
            return;
        }
    }

    final boolean nextClicked = clickedNow;

    h.postDelayed(() ->
            pollInvite(pkg, message, cb, deadline, nextClicked), 350);
}
