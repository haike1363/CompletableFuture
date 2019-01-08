package pers.haike.demo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.junit.Test;

public class AllTest {

    public static <T> CompletableFuture<List<T>> allOfList(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture = CompletableFuture
            .allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(
            Collectors.<T>toList()));
    }

    @Test
    public void testAll() throws Exception {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            return null;
        });

        CompletableFuture<Integer> future2 = CompletableFuture.completedFuture(2);

        CompletableFuture<List<Integer>> futures = allOfList(
            Arrays.asList(future1, future2));
        System.out.println(futures.get());

        CompletableFuture<Void> futures2 = CompletableFuture.allOf(future1, future2);
        future2.get();

        CompletableFuture<Object> futureAny = CompletableFuture.anyOf(future1, future2);
        futureAny.get();
    }

}
