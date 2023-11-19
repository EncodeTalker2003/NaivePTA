### Todolist

现在的版本: 51.36/70

可能可以做的优化:
- 增加Clone的层数. 在`cspta/selector`下实现了两层callSite和object的clone, 应该可以考虑增加层数.
- 特判一些 corner case. 例如throw exception之类的, 可以借助[这里的程序](https://github.com/secure-software-engineering/PointerBench/)跑些测试.
- 需要加一个计时类防止跑超时.
- 需要加一个wrapper使得在出现exception或者无法handle的情况时输出一个最trivial的结果保证soundness.
- 改成flow-sensitive的分析.


### 一些事实

- context-insensitive的在`cipta`中, context-sensitive的在`cspta`中.
- 现在已经有了field-sensitive和(简单的)context-sensitive(主要是object-sensitive).