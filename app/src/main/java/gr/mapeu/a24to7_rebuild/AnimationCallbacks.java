package gr.mapeu.a24to7_rebuild;

import android.view.animation.Animation;

class AnimationCallbacks {
    LoginScreen context;

    AnimationCallbacks(LoginScreen context) {
        this.context = context;
    }

    Animation.AnimationListener passOnAnimation = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            context.passLayout.animate().translationX(0).start();
            context.passLayout.setX(context.userLayout.getX());
            context.passLayout.setY(context.userLayout.getY());
            context.logIn.animate().translationX(0).start();
            context.back.animate().translationX(0).start();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    Animation.AnimationListener userOnAnimation = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            context.userLayout.animate().translationX(0).start();
            context.next.animate().translationX(0).start();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    Animation.AnimationListener userOffAnimation = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            context.userLayout.animate().translationX(5000).start();
            context.next.animate().translationX(5000).start();
        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
}