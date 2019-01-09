##

规则一：
对完成事件，数据，异常，不同处理方式

- when系列，消费数据及异常
- handle系列，转换数据及异常
- apply系列，转换数据
- accept系列，接受数据
- run系列，消费完成事件

compose 连续处理连续提交异步任务  

规则二：
是否含有async

- 不含async，由执行任务的当前线程执行
- 含有async，由线程池自由调度

规则三：
1. 任务是立即被调度的，但是调度的时机是不确定的，不会因为代码在前面而被先调度到  
2. 如果多个任务直接无前后依赖，他们需要对他们共同依赖的任务数据做多线程保护
3. 在消费系列函数里面尽量不要修改依赖的值