项目主要讲述利用hook拦截activity跳转（使用场景：登录逻辑跳转拦截，利用hook技术，不用每次跳转都判断是否登录）

思路：
    1、hook点有什么特点，或者是寻找hook点的原则
        说起寻找，肯定是在越表面，隐藏得越浅的较容易找到。Android中，只能是靠我们自己去查看源码，找到hook的对象。
        那什么对象比较好hook呢？自然是容易找到的对象，而什么对象容易找到呢？肯定是单例，或者是静态对象，并且这两
        种对象相对不容易发生改变的，那就容易定位了。而普通对象的不确定性太多，又会经常改变，就很难去标志。到这里，
        基本就确定了hook的对象。

    2、寻找hook点
        像这个项目的例子，hook点就是activity的跳转。
        public void toSecondActivity(View view) {
            startActivity();
        }
        点击跳转按钮，就会调用系统的startActivity方法，寻着这个方法，可以深入了解到底是哪个对象启动activity的，根据
        对源码的追踪分析，最终调用到下面的代码
        public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
                    @Nullable Bundle options) {
                if (mParent == null) {
                    options = transferSpringboardActivityOptions(options);
                    Instrumentation.ActivityResult ar =
                        mInstrumentation.execStartActivity(
                            this, mMainThread.getApplicationThread(), mToken, this,
                            intent, requestCode, options);
                    if (ar != null) {
                        mMainThread.sendActivityResult(
                            mToken, mEmbeddedID, requestCode, ar.getResultCode(),
                            ar.getResultData());
                    }
                    if (requestCode >= 0) {
                        mStartedActivity = true;
                    }

                    cancelInputsAndStartExitTransition(options);
                } else {
                    if (options != null) {
                        mParent.startActivityFromChild(this, intent, requestCode, options);
                    } else {
                        mParent.startActivityFromChild(this, intent, requestCode);
                    }
                }
            }
        上面代码，我们只关注调用了Instrumentation中的execStartActivity这个方法就可以了，这个方法的实现如下；
        ActivityManager.getService()
                        .startActivity(whoThread, who.getBasePackageName(), intent,
                                intent.resolveTypeIfNeeded(who.getContentResolver()),
                                token, target, requestCode, 0, null, options);
        从代码发现，是IActivityManager的实例调用了跳转的方法，而这个实例是通过以下方法创建的
        private static final Singleton<IActivityManager> IActivityManagerSingleton =
                    new Singleton<IActivityManager>() {
                        @Override
                        protected IActivityManager create() {
                            final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                            final IActivityManager am = IActivityManager.Stub.asInterface(b);
                            return am;
                        }
                    };
        到这里，逻辑基本清晰了，实际上是通过获取ams远程服务的binder对象，再通过asInterface转换成本地对象。我们拦截的是
        startActivity，那改变IActivityManager是一种方法，IActivityManagerSingleton是静态的，符合hook的原则，到此我们就
        已经找到了一个较好的hook点。

    3、分析应用到的知识点
        （1）上面我们看到IActivityManagerSingleton对象是私有的，而要那个这个对象，自然而然就会想到反射技术；
        （2）拿到对象后，我们要拦截里面的方法，再做自己的处理，这就会用到了动态代理。

说完思路，具体的实现逻辑在项目中体现。
