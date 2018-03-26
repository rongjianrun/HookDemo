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
        public ActivityResult execStartActivity(
                    Context who, IBinder contextThread, IBinder token, Activity target,
                    Intent intent, int requestCode, Bundle options) {
                IApplicationThread whoThread = (IApplicationThread) contextThread;
                if (mActivityMonitors != null) {
                    synchronized (mSync) {
                        final int N = mActivityMonitors.size();
                        for (int i=0; i<N; i++) {
                            final ActivityMonitor am = mActivityMonitors.get(i);
                            if (am.match(who, null, intent)) {
                                am.mHits++;
                                if (am.isBlocking()) {
                                    return requestCode >= 0 ? am.getResult() : null;
                                }
                                break;
                            }
                        }
                    }
                }
                try {
                    intent.migrateExtraStreamToClipData();
                    intent.prepareToLeaveProcess();
                    // 重点关注这个方法
                    int result = ActivityManagerNative.getDefault()
                        .startActivity(whoThread, who.getBasePackageName(), intent,
                                intent.resolveTypeIfNeeded(who.getContentResolver()),
                                token, target != null ? target.mEmbeddedID : null,
                                requestCode, 0, null, options);
                    checkStartActivityResult(result, intent);
                } catch (RemoteException e) {
                }
                return null;
            }
        上面代码，我们只关注调用了Instrumentation中的execStartActivity这个方法就可以了
        从代码发现，是ActivityManagerNative.getDefault()调用了跳转的方法，而这个实例是通过以下方法创建的
        static public IActivityManager getDefault() {
                return gDefault.get();
            }
        private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
                protected IActivityManager create() {
                    IBinder b = ServiceManager.getService("activity");
                    if (false) {
                        Log.v("ActivityManager", "default service binder = " + b);
                    }
                    IActivityManager am = asInterface(b);
                    if (false) {
                        Log.v("ActivityManager", "default service = " + am);
                    }
                    return am;
                }
            };
        到这里，逻辑基本清晰了，实际上是通过获取ams远程服务的binder对象，再通过asInterface转换成本地对象。我们拦截的是
        startActivity，那改变IActivityManager是一种方法，gDefault是静态的，符合hook的原则，到此我们就已经找到了一个较好
        的hook点。

    3、分析应用到的知识点
        （1）上面我们看到gDefault对象是私有的，而要那个这个对象，自然而然就会想到反射技术；
        （2）拿到对象后，我们要拦截里面的方法，再做自己的处理，这就会用到了动态代理。

说完思路，具体的实现逻辑在项目中体现。
