package pers.haike.demo;


import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static Random rand = new Random();
    private static long t = System.currentTimeMillis();

    static int getMoreData() {
        System.out.println("begin to start compute");
        try {
            Thread.sleep(1000);
            if (rand.nextInt(2) == 1) {
                throw new Exception("123");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(
            "end to start compute. passed " + (System.currentTimeMillis() - t) / 1000 + " seconds");
        return rand.nextInt(1000);
    }

    public static void main(String[] args) throws Exception {
        //  testThen();
        testCompose();
        System.in.read();
    }

    /*
     * Runnable类型的参数会忽略计算的结果
     * Consumer是纯消费计算结果，BiConsumer会组合另外一个CompletionStage纯消费
     * Function会对计算结果做转换，BiFunction会组合另外一个CompletionStage的计算结果做转换
     * */
    public static void testThen() throws Exception {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Main::getMoreData);
        CompletableFuture<String> f = future.whenComplete((v, e) -> {
            System.out.println(v);
            System.out.println("whenComplete: " + e);
            // e不空，此异常将被e覆盖掉,如果e为空，exceptionally将捕捉到此异常
            // throw new RuntimeException("whenComplete RuntimeException");
        }).exceptionally((e) -> {
            System.out.println("exceptionally: " + e);
            // 此异常会被f.get()捕捉到
            // throw new RuntimeException("exceptionally RuntimeException");
            return 0;
        }).handle((v, e) -> {
            return (double) (v + 100);
        }).thenApply((v) -> {
            return "string: " + v;
        });

        f.thenAccept((s) -> {
            System.out.println("accept: " + s);
        });

        future.thenAcceptBoth(f, (i, s) -> {
            System.out.println("thenAcceptBoth:" + i + s);
        }).runAfterBoth(f, () -> {
            System.out.println("runAfterBoth");
        });

        System.out.println(f.get());
    }

    public static void testCompose() throws Exception {

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            return 100;
        });
        CompletableFuture<String> f = future.thenCompose(i -> {
            // do something
            return CompletableFuture.supplyAsync(() -> {
                return (i * 10) + "";
            });
        });
        System.out.println(f.get());
    }

    public static void testCombine() throws Exception {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            return 100;
        });
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            return "abc";
        });
        CompletableFuture<String> f = future.thenCombine(future2, (x, y) -> y + "-" + x);
        System.out.println(f.get()); //abc-100
    }

    public static void testEither() throws Exception {
        Random rand = new Random();
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000 + rand.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 100;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000 + rand.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 200;
        });
        CompletableFuture<String> f = future.applyToEither(future2, (s) -> {
            System.out.println(s);
            return "s/2: " + s / 2;
        });
    }

    public static void testAnyAll() throws Exception {
        Random rand = new Random();
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000 + rand.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 100;
        });
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000 + rand.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "abc";
        });
        // future类型可以不同
        CompletableFuture<Void> f =  CompletableFuture.allOf(future1,future2);
        CompletableFuture<Object> f2 = CompletableFuture.anyOf(future1, future2);
        System.out.println(f.get());
    }


}
