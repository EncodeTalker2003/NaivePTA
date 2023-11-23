### Todolist(按照优先级从高到低排序)

现在的版本: 56.61/70

可能可以做的优化:
- 需要加一个计时类防止跑超时.
  - upd(11.21): 可以直接throw一个exception给wrapper.
  - upd(11.22): 28号测试点看起来会跑很久, 不知道是不是什么暴力函数调用之类的
- 需要加一个wrapper使得在出现exception或者无法handle的情况时输出一个最trivial的结果保证soundness.
  - upd(11.21): wrapper已经写好了, 就是需要在分析中加上跳转到wrapper的分支就行
  - upd(11.21): 这个需要更多的测试
- 确认一下框架中的Monitor语句对程序的影响是什么(看起来是和同步相关的, 至少不影响当前测试样例成绩)
- 增加Clone的层数. 在`cspta/selector`下实现了两层callSite和object的clone, 应该可以考虑增加层数.
  - upd(11.21): 笑死, 调层数和调参一样, 感觉分析可能鲁棒性不是特别强
- 特判一些 corner case. 例如throw exception之类的, 可以借助[这里的程序](https://github.com/secure-software-engineering/PointerBench/)跑些测试.
  - upd(11.22): 已知MultiArray是无法处理的, 暂时考虑直接抛异常

- 改成flow-sensitive的分析.


### 一些事实

- context-insensitive的在`cipta`中, context-sensitive的在`cspta`中.
- 现在已经有了field-sensitive和(简单的)context-sensitive(主要是object-sensitive).


### 如何运行

- 如果没有`java-benchmarks`这个文件夹的话需要按照ppt操作给clone下来.
- 使用命令`gradle run --args="-a pku-pta -cp src/test/pku/ -m test.FieldSensitivity"`进行测试
  - `-a` 指出使用什么分析, 可以在`\src\main\resources\tai-e-analyses.yml`查找对应信息.
  - `-cp`指出测试的文件夹, 一般不用改.
  - `-m`指出测试的类.
- 使用命令`gradle fatJar`在`build`文件夹下生成jar文件. 