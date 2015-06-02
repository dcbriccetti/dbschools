// https://developer.mozilla.org/en-US/docs/Web/API/notification

function sendNotification(msg) {
    // Let's check if the browser supports notifications
    if (!("Notification" in window)) {
        alert("This browser does not support desktop notification");
    }

    // Let's check if the user is okay to get some notification
    else if (Notification.permission === "granted") {
        // If it's okay let's create a notification
        var notification = new Notification(msg);
        setNotificationTimeout(notification);
    }

    // Otherwise, we need to ask the user for permission
    // Note, Chrome does not implement the permission static property
    // So we have to check for NOT 'denied' instead of 'default'
    else if (Notification.permission !== 'denied') {
        Notification.requestPermission(function (permission) {
            // Whatever the user answers, we make sure we store the information
            if (!('permission' in Notification)) {
                Notification.permission = permission;
            }

            // If the user is okay, let's create a notification
            if (permission === "granted") {
                var notification = new Notification(msg);
                setNotificationTimeout(notification);
                return notification;
            }
        });
    }

    // At last, if the user already denied any notification, and you
    // want to be respectful there is no need to bother them any more.
}

function setNotificationTimeout(notification) {
    function cancelNotification() {
        notification.close();
    }

    setTimeout(cancelNotification, 30000);
}
