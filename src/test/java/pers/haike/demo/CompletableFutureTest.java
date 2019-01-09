package pers.haike.demo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.junit.Test;



public class CompletableFutureTest {

    static void sleep(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (Throwable e) {
        }
    }

    static void print(String str) {
        System.out.println("tid:" + Thread.currentThread().getId()
                + " " + str);
    }

    public static <T> CompletableFuture<List<T>> allOfList(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(
                Collectors.<T>toList()));
    }

    @Test
    public void testGet() throws Exception {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            return 123;
        });
        System.out.println(
                future.getNow(456));
        future.join();
        System.out.println(
                future.get());
        System.out.println(
                future.get(1, TimeUnit.SECONDS));

        // 作为promise传递
        final CompletableFuture<Integer> signal = new CompletableFuture<>();
        ForkJoinPool.commonPool().submit(() -> {
            signal.complete(789);
            // signal.completeExceptionally(new Exception("aaa"))
        });
        System.out.println(
                signal.get());
    }

    @Test
    public void testRunAndSupply() {
        Executor executor = Executors.newCachedThreadPool();
        CompletableFuture<Void> futureRun = CompletableFuture.runAsync(() -> {

        }, executor).exceptionally((e) -> { // 异常处理
            return null;
        });

        CompletableFuture<Integer> futureSupply = CompletableFuture.supplyAsync(() -> {
            return 123;
        });
    }

    static class Value {
        public Value() {
            s = "init";
        }
        public Value(String s) {
            this.s = s;
        }
        public String s;

        @Override
        public String toString() {
            return " Value{s:" + s + ",hash:" + hashCode() + "} ";
        }
    }

    static class Result {
        public Result() {
            s = "init";
        }
        public Result(String s) {
            this.s = s;
        }
        public Result(Value v) {
            this.s = v.s;
        }
        public String s;

        @Override
        public String toString() {
            return " Result{s:" + s + ",hash:" + hashCode() + "} ";
        }
    }

    @Test
    public void testWhenComplete() throws Exception {
        // 触发立即被调度
        CompletableFuture<Value> future = CompletableFuture.supplyAsync(() -> {
            print("supply process");
            return new Value("supply processed");
        });

        // 没有加async则使用相同线程，可处理异常
        CompletableFuture<Value> futureWhen = future.whenComplete((v, e) -> {
            print("whenComplete process " + v);
            // 最好不要修改v
            v.s = "whenComplete processed";
        });

        sleep(1);
        // get的值不确定，可能when的处理已经执行
        print("get1 " + future.get());
        // 如果有连续处理的逻辑，最好使用最后的futureWhen
        // 因为中间的处理逻辑可能会修改对象
        print("whenget " + futureWhen.get());
        print("get2 " + future.get());


        // 如果有async则由线程池调度
        // 立即进行调度执行，但是什么时候被调度到，无法确定
        // 2个when没有依赖，是有可能后面的when先被调度到
        future.whenCompleteAsync((v, e) -> {
            print("whenCompleteAsync process" + v);
            v.s = "whenCompleteAsync processed";
        });
        sleep(2);
    }

    @Test
    public void testHandle() throws Exception {
        CompletableFuture<Value> future = CompletableFuture.supplyAsync(() -> {
            print("supply process");
            return new Value("supply processed");
        });
        // 转换为另外一个返回值，可处理异常
        CompletableFuture<Result> futureHandle = future.handle((v, e) -> {
            print("handle process " + v);
            // 最好不要修改v的值
            v.s = "handle processed";
            return new Result("handle processed");
        });

        sleep(1);
        // get的值不确定，可能handle的处理已经执行
        print("get " + future.get());
        print("handleget " + futureHandle.get());

        sleep(2);
    }

    @Test
    public void testThenApply() throws Exception {
        CompletableFuture<Value> future = CompletableFuture.supplyAsync(() -> {
            print("supply process");
            return new Value("supply processed");
        });

        // 转换为另外一个返回值，不能处理异常
        // 用于为之前的supply做补充
        CompletableFuture<Result> futureThenApply = future.thenApply((v) -> {
            print("thenApply process " + v);
            // 最好不要修改v的值
            v.s = "thenApply processed";
            return new Result("thenApply processed");
        });

        print("thenApplyet " + futureThenApply.get());
        sleep(1);
    }

    @Test
    public void testThenAccept() throws Exception {
        CompletableFuture<Value> future = CompletableFuture.supplyAsync(() -> {
            print("supply process");
            return new Value("supply processed");
        });

        // 纯消费正常数据，不能处理异常，不能返回其他值
        CompletableFuture<Void> futureThenAccept = future.thenAccept((v) -> {
            print("thenApply process " + v);
            // 最好不要修改v的值
            v.s = "thenApply processed";
        });
        // 不使用正常结果
        future.thenRun(()-> {
            print("thenRun process");
        });

        print("thenApplyet " + futureThenAccept.get());
        sleep(1);
    }

    @Test
    public void testThenAcceptBoth() throws Exception {
        CompletableFuture<Value> future = CompletableFuture.supplyAsync(() -> {
            print("supply process value");
            return new Value("supply processed");
        });
        CompletableFuture<Result> futureResult = CompletableFuture.supplyAsync(() -> {
            print("supply process result");
            return new Result("supply processed");
        });

        // 纯消费2个正常数据，不能处理异常，不能返回其他值
        CompletableFuture<Void> futureThenAcceptBoth = future.thenAcceptBoth(
                futureResult, (v, r) -> {
            print("thenAcceptBoth process " + v + r);
        });
        // 不使用正常结果
        future.runAfterBoth(futureResult, () -> {
            print("runAfterBoth process");
        });

        print("thenAcceptBoth " + futureThenAcceptBoth.get());
        sleep(1);
    }

    @Test
    public void testCompose () throws Exception {
        CompletableFuture<Value> future = CompletableFuture.supplyAsync(() -> {
            print("supply process value");
            return new Value("supply processed");
        });

        // 异步处理后，连续提交异步操作
        CompletableFuture<Result> futureResult = future.thenCompose((v) -> {
            print("thenCompose compose value");
            // 返回一个异步组合
            return CompletableFuture.supplyAsync(() -> {
                print("supply process result by value");
                return new Result(v);
            });
        });

        print("thenComposeGet " + futureResult.get());
        sleep(1);
    }


    @Test
    public void testEither () throws Exception {
        CompletableFuture<Value> future1 = CompletableFuture.supplyAsync(() -> {
            print("supply process value1");
            return new Value("1 supply processed");
        });
        CompletableFuture<Value> future2 = CompletableFuture.supplyAsync(() -> {
            print("supply process value2");
            return new Value("2 supply processed");
        });

        CompletableFuture<Void> futureAccept = future1.acceptEither(future2, (v) -> {
            print("acceptEither " + v);
        });
        print("acceptEitherGet " + futureAccept.get());

        CompletableFuture<Result> futureApply = future1.applyToEither(future2, (v) -> {
            return new Result(v);
        });
        print("applyToEitherGet " + futureAccept.get());
        sleep(1);
    }

    @Test
    public void testAllOfAnyOf() throws Exception {
        CompletableFuture<Value> future1 = CompletableFuture.supplyAsync(() -> {
            print("supply process value1");
            return new Value("1 supply processed");
        });
        CompletableFuture<Value> future2 = CompletableFuture.supplyAsync(() -> {
            print("supply process value2");
            return new Value("2 supply processed");
        });
        CompletableFuture<Result> futureResult = CompletableFuture.supplyAsync(() -> {
            print("supply process result");
            return new Result("supply processed");
        });

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(future1, futureResult);
        print("allOf get " + allFuture.get());

        CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(future1, futureResult);
        print("anyOf get " + anyFuture.get());

        CompletableFuture<List<Value>> allOfList = allOfList(Arrays.asList(future1, future2));
        print("allOfList get " + Arrays.toString(allOfList.get().toArray()));
        sleep(1);
    }
}
