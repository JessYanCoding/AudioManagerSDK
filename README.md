#AudioManager SDK
## Introduction
 `AudioManager`专注于录音相关逻辑的处理，目前举手发言场景和点名发言场景已经使用`AudioManager`，后续有其他场景要使用到录音相关功能都应使用此类
##Dev tips
* `AudioManager`独立于举手发言场景，它不与举手发言场景耦合，唯一与举手场景耦合的地方时录音结束后发送消息给主播，每个场景发送的逻辑不一样，所以采用接口的方式(post interface)，让调用者实现，此场景下由`RaiseManager`实现.

* 因为录音相关逻辑涉及到上传和下载的功能，`AudioManager`专注于录音处理，由于网络框架每个项目都有自己独有的一套，且庞大，所以调用者通过`HttpHelper`接口，自己实现上传下载的功能,这样保证了扩展又保证了**SDK**的大小.
* `AudioInfo`表示录音的消息，里面封装有基本的信息，为了扩展，`AudioManager`可以传入一个继承于`AudioInfo`的范型，这样就可以在回调中收到自己定义的录音消息对象，可以储存一些自己定义的信息. 

## 参数及方法介绍:
###构建`AudioManager `需要传入的参数 
 * 必要参数
  * **Context**: 上下文
  * **HttpHelper**: 用于上传下载录音的接口，每个应用场景必须自己实现
 * 非必要参数
  * **MaxRecordTime**: 单次录音最大录制时间，超时会在AudioListener中回调
  * **MinRecordTime**: 单次录音最小录音时间，超时会在AudioListener中回调
  * **AudioListener**: 录音时的一些信息回调(与MaxRecordTime和MinRecordTime有关)
  * **StatusListener**: 对于录音信息的一些状态回调(录音上传中，播放中，下载失败...)

### 主要方法
 * **startRecord**: 开始录制语音
 * **stopRecord**: 停止录制语音，将当前录制的语音自动加入到队列中
 * **cancel**: 取消录制
 * **startUpload**: 开始上传录音，指定上传某个录音，也可以自动取队列的首个录音上传，如果上传失败他会在后台自动续传
 * **download**: 下载指定的录音
 * **addSpeech**: `AudioManager `自己管理一个用于展示到**UI**的**List**,此方法为添加到此列表
 * **getSpeechList**: 获得用于展示到**UI**的**List**，比如给**RecycleView**使用
 * **play**: 播放录音,内部有一个队列，如果在一个录音播放完之前被调用多次，则后面加入的录音都在队列中，等待播放，播放完上一个录音会检测队列，有等待的录音则继续播放下一个
 * **palySingle**: 播放录音，内部没有队列，调用时如果之前的录音还没播放完则停止之前的录音，立即开始现在录音的播放
 * **stopPlay**: 停止播放所有录音
 * **release**: 释放资源

###示例代码
    
    mAudioManager = AudioManager
                .builder()
                .httpHelper(new OkHttpHelper(getActivity()))
                .minRecordTime(2, TimeUnit.SECONDS)
                .with(getActivity())
                .addAudioListener(AudioListener)
                .addStatusListener(StatusListener)
                .build();

## 详解
### AudioInfo structure

Field       | Description
:----------:|:-------------:
filePath    | 录音文件本地路径
url         | 上传到服务器的url地址
recordTime  | 录制的时间单位秒
ctime       | 创建的时间单位毫秒，作为此录音的唯一码
status      | 0.正常状态 1.下载中 2.播放失败 3.播放中 4.播放暂停

### AudioListener Introduction
Method               |  Description
:-------------------:|:-------------:
onRecordTooShort     | 单次录制时间少于MinRecordTime，则调用此回调
onRecordTooLong      | 单次录制时间大于MaxRecordTime，则调用此回调
onVoiceAmplitudeLevel| 在录制时会执行此回调，会提供声音的大小，可以做一些**UI**操作

### StatusListener Introduction
录音状态信息有变化则会调用此回调(如录音下载成功，失败...)，回调提供一个继承于`AudioInfo`的范型(可自己指定，便于存放一些自定义消息)以及一个状态码，告诉开发者当前录音的状态

StatusCode | Description
:---------:|:--------------:
0          | 此录音下载中
1          | 此录音下载失败
2          | 此录音下载成功
3          | 此录音播放中
4          | 此录音播放失败
5          | 此录音播放完成
6          | 此录音被加入到列表中
7          | 此录音在播放中被暂停

###Post Interface  
所有应用场景必须实现它，上传成功后便会调用它发送im消息，每个场景自定义自己的逻辑 

###Executor Interface
此接口是处理自动续传逻辑，即上传失败后，后台保持队列继续上传直到成功，默认实现为HttpExecutor,可以实现`Executor Interface`自定义自己的自动续传逻辑. 

###HttpHelper Interface
此接口是上传下载录音的接口，没有默认实现，如果要想上传下载录音必须，自己实现，原因上面已经提及

###Audio Interface
`AudioManager`就实现于此接口，如果你觉得此录音管理类写的太渣了，你完全可以实现此接口重新写相关的逻辑，完全不用改之前的所有代码，此**SDK**大量使用接口处理扩展和低耦合，后面的场景应该是用此开发模式
