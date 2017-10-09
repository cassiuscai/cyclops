package cyclops.control.lazy;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher3;
import com.aol.cyclops2.matching.Sealed3;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.factory.Unit;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.BiTransformable;
import com.aol.cyclops2.types.functor.Transformable;
import com.aol.cyclops2.types.reactive.Completable;
import cyclops.async.Future;
import cyclops.collections.mutable.ListX;
import cyclops.control.*;
import cyclops.function.*;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.either3;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.comonad.ComonadByPure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;
import cyclops.collections.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;
/**
 * A right biased Lazy Either3 type. transform / flatMap operators are tail-call optimized
 * 
 * Can be one of 3 types
 * 
 * 
 * 
 * @author johnmcclean
 *
 * @param <LT1> Left1 type
 * @param <LT2> Left2 type
 * @param <RT> Right type (operations are performed on this type if present)
 */
public interface Either3<LT1, LT2, RT> extends  Value<RT>,
                                                OrElseValue<RT,Either3<LT1,LT2,RT>>,
                                                Unit<RT>,
                                                Transformable<RT>,
                                                Filters<RT>,
                                                BiTransformable<LT2, RT>,
                                                To<Either3<LT1, LT2, RT>>,
                                                Supplier<RT>,
                                                Sealed3<LT1,LT2,RT>,
                                                Higher3<either3,LT1,LT2,RT> {

    public static  <LT1,LT2,T> Kleisli<Higher<Higher<either3, LT1>, LT2>,Either3<LT1,LT2,T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(), Either3::widen);
    }
    public static <LT1,LT2,T> Higher<Higher<Higher<either3, LT1>, LT2>,T> widen(Either3<LT1,LT2,T> narrow) {
        return narrow;
    }
    public static  <LT1,LT2,T> Cokleisli<Higher<Higher<either3, LT1>,LT2>,T,Either3<LT1,LT2,T>> kindCokleisli(){
        return Cokleisli.of(Either3::narrowK);
    }


    default <T2, R> Either3<LT1, LT2,R> zip(final Either3<LT1, LT2,? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)));
    }

    /**
     * Create a reactiveBuffer CompletableEither
     *
     * <pre>
     *  {@code
     *      ___Example 1___
     *
     *      CompletableEither<Integer,Integer> completable = Either3.either3();
            Either3<Throwable,String,Integer> mapped = completable.transform(i->i*2)
                                                                  .flatMap(i->Eval.later(()->i+1));

            completable.complete(5);

            mapped.printOut();
            //11

            ___Example 2___

            CompletableEither<Integer,Integer> completable = Either3.either3();
            Either3<Throwable,String,Integer> mapped = completable.transform(i->i*2)
                                                                  .flatMap(i->Eval.later(()->i+1));


            completable.complete(null);

            //Either3:Left3[NoSuchElementException]

            ___Example 3___

            CompletableEither<Integer,Integer> completable = Either3.either3();
            Either3<Throwable,String,Integer> mapped = completable.transform(i->i*2)
                                                                 .flatMap(i->Eval.later(()->i+1));

            completable.complete(new IllegalStateException());

    //Either:Left[IllegalStateElementException]
     *     }
     * </pre>
     *
     * @param <RT>
     * @return
     */
    static <LT2,RT> Either3.CompletableEither3<RT,LT2,RT> either3(){
        Completable.CompletablePublisher<RT> c = new Completable.CompletablePublisher<RT>();
        return new Either3.CompletableEither3<RT,LT2, RT>(c,fromFuture(Future.fromPublisher(c)));
    }
    @AllArgsConstructor
    static class CompletableEither3<ORG,LT1,RT> implements Either3<Throwable,LT1,RT>, Completable<ORG> {

        public final Completable.CompletablePublisher<ORG> complete;
        public final Either3<Throwable, LT1,RT> either;

        @Override
        public boolean isFailed() {
            return complete.isFailed();
        }

        @Override
        public boolean isDone() {
            return complete.isDone();
        }

        @Override
        public boolean complete(ORG v) {
            return complete.complete(v);
        }

        @Override
        public boolean completeExceptionally(Throwable error) {
            return complete.completeExceptionally(error);
        }

        @Override
        public RT get() {
            return either.get();
        }

        @Override
        public <R> R visit(Function<? super Throwable, ? extends R> left1, Function<? super LT1, ? extends R> mid, Function<? super RT, ? extends R> right) {
            return either.visit(left1,mid,right);
        }

        @Override
        public Maybe<RT> filter(Predicate<? super RT> test) {
            return either.filter(test);
        }

        @Override
        public <R2> Either3<Throwable, LT1, R2> flatMap(Function<? super RT, ? extends Either3<Throwable,LT1,? extends R2>> mapper) {
            return either.flatMap(mapper);
        }

        @Override
        public Either3<Throwable, RT, LT1> swap2() {
            return either.swap2();
        }

        @Override
        public Either3<RT, LT1, Throwable> swap1() {
            return either.swap1();
        }

        @Override
        public boolean isRight() {
            return either.isRight();
        }

        @Override
        public boolean isLeft1() {
            return either.isLeft1();
        }

        @Override
        public boolean isLeft2() {
            return either.isLeft2();
        }

        @Override
        public <R1, R2> Either3<Throwable, R1, R2> bimap(Function<? super LT1, ? extends R1> fn1, Function<? super RT, ? extends R2> fn2) {
            return either.bimap(fn1,fn2);
        }

        @Override
        public <R> Either3<Throwable, LT1, R> map(Function<? super RT, ? extends R> fn) {
            return either.map(fn);
        }

        @Override
        public <T> Either3<Throwable, LT1, T> unit(T unit) {
            return either.unit(unit);
        }

        @Override
        public <R> R fold(Function<? super Throwable, ? extends R> fn1, Function<? super LT1, ? extends R> fn2, Function<? super RT, ? extends R> fn3) {
            return either.fold(fn1,fn2,fn3);
        }

        @Override
        public <R> R visit(Function<? super RT, ? extends R> present, Supplier<? extends R> absent) {
            return either.visit(present,absent);
        }
    }

    static <LT1,LT2,RT> Either3<LT1,LT2,RT> fromLazy(Eval<Either3<LT1,LT2,RT>> lazy){
        return new Either3.Lazy<>(lazy);
    }

    static <LT2,RT> Either3<Throwable,LT2,RT> fromFuture(Future<RT> future){
        return fromLazy(Eval.<Either3<Throwable,LT2,RT>>fromFuture(
                future.map(e->e!=null?Either3.<Throwable,LT2,RT>right(e) : Either3.<Throwable,LT2,RT>left1(new NoSuchElementException()))
                        .recover(t->Either3.<Throwable,LT2,RT>left1(t.getCause()))));
    }
    /**
     *  Turn a toX of Either3 into a singleUnsafe Either with Lists of values.
     *
     * <pre>
     * {@code
     *
     * Either3<String,String,Integer> just  = Either3.right(10);
       Either3<String,String,Integer> none = Either3.left("none");


     * Either3<ListX<String>,ListX<String>,ListX<Integer>> xors =Either3.sequence(ListX.of(just,none,Either3.right(1)));
       //Eitehr.right(ListX.of(10,1)));
     *
     * }</pre>
     *
     *
     *
     * @param Either3 Either3 to sequence
     * @return Either3 Sequenced
     */
    public static <LT1,LT2, PT> Either3<ListX<LT1>,ListX<LT2>,ListX<PT>> sequence(final CollectionX<Either3<LT1, LT2, PT>> xors) {
        Objects.requireNonNull(xors);
        return AnyM.sequence(xors.stream().filter(Either3::isRight).map(AnyM::fromEither3).to().listX(), either3.INSTANCE)
                .to(Witness::either3);
    }
    /**
     * TraverseOps a Collection of Either3 producing an Either3 with a ListX, applying the transformation function to every
     * element in the list
     *
     * @param xors Either3s to sequence and transform
     * @param fn Transformation function
     * @return An Either3 with a transformed list
     */
    public static <LT1,LT2, PT,R> Either3<ListX<LT1>,ListX<LT2>,ListX<R>> traverse(final CollectionX<Either3<LT1, LT2, PT>> xors, Function<? super PT, ? extends R> fn) {
        return  sequence(xors).map(l->l.map(fn));
    }


    /**
     *  Accumulate the results only from those Either3 which have a Right type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see com.aol.cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     * Either3<String,String,Integer> just  = Either3.right(10);
       Either3<String,String,Integer> none = Either3.left("none");
     *
     *  Either3<ListX<String>,ListX<String>,Integer> xors = Either3.accumulatePrimary(Monoids.intSum,ListX.of(just,none,Either3.right(1)));
        //Either3.right(11);
     *
     * }
     * </pre>
     *
     *
     *
     * @param xors Collection of Eithers to accumulate primary values
     * @param reducer  Reducer to accumulate results
     * @return  Either3 populated with the accumulate primary operation
     */
    public static <LT1,LT2, RT> Either3<ListX<LT1>, ListX<LT2>, RT> accumulate(final Monoid<RT> reducer, final CollectionX<Either3<LT1, LT2, RT>> xors) {
        return sequence(xors).map(s -> s.reduce(reducer));
    }


    /**
     * Lazily construct a Right Either from the supplied publisher
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

         Either3<Throwable,String,Integer> future = Either3.fromPublisher(reactiveStream);

         //Either[1]
     *
     * }
     * </pre>
     * @param pub Publisher to construct an Either from
     * @return Either constructed from the supplied Publisher
     */
    public static <T1,T> Either3<Throwable, T1, T> fromPublisher(final Publisher<T> pub) {
        return fromFuture(Future.fromPublisher(pub));
    }
    /**
     * Construct a Right Either3 from the supplied Iterable
     * <pre>
     * {@code
     *   List<Integer> list =  Arrays.asList(1,2,3);

         Either3<Throwable,String,Integer> future = Either3.fromIterable(list);

         //Either[1]
     *
     * }
     * </pre>
     * @param iterable Iterable to construct an Either from
     * @return Either constructed from the supplied Iterable
     */
    public static <ST, T,RT> Either3<ST, T,RT> fromIterable(final Iterable<RT> iterable) {

        final Iterator<RT> it = iterable.iterator();
        return it.hasNext() ? Either3.right( it.next()) : Either3.left1(null);
    }

    /**
     * Static method useful as a method reference for fluent consumption of any value type stored in this Either
     * (will capture the lowest common type)
     *
     * <pre>
     * {@code
     *
     *   myEither.to(Either3::consumeAny)
                 .accept(System.out::println);
     * }
     * </pre>
     *
     * @param either Either to consume value for
     * @return Consumer we can applyHKT to consume value
     */
    static <X, LT extends X, M extends X, RT extends X>  Consumer<Consumer<? super X>> consumeAny(Either3<LT, M, RT> either){
        return in->visitAny(in,either);
    }

    static <X, LT extends X, M extends X, RT extends X,R>  Function<Function<? super X, R>,R> applyAny(Either3<LT, M, RT> either){
        return in->visitAny(either,in);
    }
    static <X, LT extends X, M extends X, RT extends X,R> R visitAny(Either3<LT, M, RT> either, Function<? super X, ? extends R> fn){
        return either.visit(fn, fn,fn);
    }
    static <X, LT extends X, M extends X, RT extends X> X visitAny(Consumer<? super X> c, Either3<LT, M, RT> either){
        Function<? super X, X> fn = x ->{
            c.accept(x);
            return x;
        };
        return visitAny(either,fn);
    }
    /**
     * Construct a Either3#Right from an Eval
     *
     * @param right Eval to construct Either3#Right from
     * @return Either3 right instance
     */
    public static <LT, B, RT> Either3<LT, B, RT> rightEval(final Eval<RT> right) {
        return new Right<>(
                           right);
    }

    /**
     * Construct a Either3#Left1 from an Eval
     *
     * @param left Eval to construct Either3#Left from
     * @return Either3 left instance
     */
    public static <LT, B, RT> Either3<LT, B, RT> left1Eval(final Eval<LT> left) {
        return new Left1<>(
                          left);
    }

    /**
     * Construct a Either3#Right
     *
     * @param right Value to store
     * @return Either3 Right instance
     */
    public static <LT, B, RT> Either3<LT, B, RT> right(final RT right) {
        return new Right<>(
                           Eval.later(()->right));
    }

    /**
     * Construct a Either3#Left1
     *
     * @param left Value to store
     * @return Left instance
     */
    public static <LT, B, RT> Either3<LT, B, RT> left1(final LT left) {
        return new Left1<>(
                          Eval.now(left));
    }

    /**
     * Construct a Either3#Left2
     *
     * @param left2 Value to store
     * @return Left2 instance
     */
    public static <LT, B, RT> Either3<LT, B, RT> left2(final B middle) {
        return new Left2<>(
                            Eval.now(middle));
    }

    /**
     * Construct a Either3#Left2 from an Eval
     *
     * @param middle Eval to construct Either3#middle from
     * @return Either3 Left2 instance
     */
    public static <LT, B, RT> Either3<LT, B, RT> left2Eval(final Eval<B> middle) {
        return new Left2<>(
                            middle);
    }

    /**
     * Visit the types in this Either3, only one user supplied function is executed depending on the type
     *
     * @param left1 Function to execute if this Either3 is a Left instance
     * @param mid Function to execute if this Either3 is a middle instance
     * @param right Function to execute if this Either3 is a right instance
     * @return Result of executed function
     */
    <R> R visit(final Function<? super LT1, ? extends R> left1, final Function<? super LT2, ? extends R> mid,
                final Function<? super RT, ? extends R> right);

    /**
     * Filter this Either3 resulting in a Maybe#none if it is not a Right instance or if the predicate does not
     * hold. Otherwise results in a Maybe containing the current value
     *
     * @param test Predicate to applyHKT to filter this Either3
     * @return Maybe containing the current value if this is a Right instance and the predicate holds, otherwise Maybe#none
     */
    Maybe<RT> filter(Predicate<? super RT> test);


    /**
     * Flattening transformation on this Either3. Contains an internal trampoline so will convert tail-recursive calls
     * to iteration.
     *
     * @param mapper Mapping function
     * @return Mapped Either3
     */
    <R2> Either3<LT1, LT2, R2> flatMap(Function<? super RT, ? extends Either3<LT1,LT2,? extends R2>> mapper);




    /**
     * @return Swap the middle and the right types
     */
    Either3<LT1, RT, LT2> swap2();

    /**
     * @return Swap the right and left types
     */
    Either3<RT, LT2, LT1> swap1();

    /**
     * @return True if this lazy contains the right type
     */
    boolean isRight();

    /**
     * @return True if this lazy contains the left1 type
     */
    boolean isLeft1();

    /**
     * @return True if this lazy contains the left2 type
     */
    boolean isLeft2();

    /**
     * Return an Ior that can be this object or a Ior.primary or Ior.secondary
     * @return new Ior
     */
     default Ior<LT1, RT> toIor() {
        return this.visit(l->Ior.secondary(l),
                          m->Ior.secondary(null),
                          r->Ior.primary(r));
    }
     default Xor<LT1, RT> toXor() {
         return this.visit(l->Xor.secondary(l),
                           m->Xor.secondary(null),
                           r->Xor.primary(r));
     }




    @Override
    default <U> Maybe<U> ofType(Class<? extends U> type) {
        return (Maybe<U> )Filters.super.ofType(type);
    }


    @Override
    default Maybe<RT> filterNot(Predicate<? super RT> predicate) {
        return (Maybe<RT>)Filters.super.filterNot(predicate);
    }

    default Trampoline<Either3<LT1,LT2,RT>> toTrampoline() {
        return Trampoline.more(()->Trampoline.done(this));
    }


    @Override
    default Maybe<RT> notNull() {

        return (Maybe<RT>)Filters.super.notNull();
    }



    @Override
    default <R> Either3<LT1,LT2,R> retry(final Function<? super RT, ? extends R> fn) {
        return (Either3<LT1,LT2,R>)Transformable.super.retry(fn);
    }


    @Override
    default <R> Either3<LT1,LT2,R> retry(final Function<? super RT, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (Either3<LT1,LT2,R>)Transformable.super.retry(fn,retries,delay,timeUnit);
    }


    default <R> Either3<LT1,LT2,R> coflatMap(Function<? super Either3<LT1,LT2,RT>, R> mapper) {

        return mapper.andThen(r -> unit(r))
                .apply(this);
    }

    default Either3<LT1,LT2,Either3<LT1,LT2,RT>> nest() {

        return this.map(t -> unit(t));
    }

    default <T2, R1, R2, R3, R> Either3<LT1,LT2,R> forEach4(Function<? super RT, ? extends Either3<LT1,LT2,R1>> value1,
                                                            BiFunction<? super RT, ? super R1, ? extends Either3<LT1,LT2,R2>> value2,
                                                            Function3<? super RT, ? super R1, ? super R2, ? extends Either3<LT1,LT2,R3>> value3,
                                                            Function4<? super RT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            Either3<LT1,LT2,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                Either3<LT1,LT2,R2> b = value2.apply(in,ina);
                return b.flatMap(inb-> {
                    Either3<LT1,LT2,R3> c= value3.apply(in,ina,inb);
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }


    default <T2, R1, R2, R> Either3<LT1,LT2,R> forEach3(Function<? super RT, ? extends Either3<LT1,LT2,R1>> value1,
                                                        BiFunction<? super RT, ? super R1, ? extends Either3<LT1,LT2,R2>> value2,
                                                        Function3<? super RT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            Either3<LT1,LT2,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                Either3<LT1,LT2,R2> b = value2.apply(in,ina);
                return b.map(in2->yieldingFunction.apply(in,ina, in2));
            });

        });
    }

    default <R1, R> Either3<LT1,LT2,R> forEach2(Function<? super RT, ? extends Either3<LT1,LT2,R1>> value1,
                                                BiFunction<? super RT, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {
            Either3<LT1,LT2,R1> b = value1.apply(in);
            return b.map(in2->yieldingFunction.apply(in, in2));
        });
    }


    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.functor.BiTransformable#bimap(java.util.function.Function,
     * java.util.function.Function)
     */
    @Override
    <R1, R2> Either3<LT1, R1, R2> bimap(Function<? super LT2, ? extends R1> fn1, Function<? super RT, ? extends R2> fn2);

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Functor#transform(java.util.function.Function)
     */
    @Override
    <R> Either3<LT1, LT2, R> map(Function<? super RT, ? extends R> fn);






    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Pure#unit(java.lang.Object)
     */
    @Override
    <T> Either3<LT1, LT2, T> unit(T unit);


    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.functor.BiTransformable#bipeek(java.util.function.Consumer,
     * java.util.function.Consumer)
     */
    @Override
    default Either3<LT1, LT2, RT> bipeek(final Consumer<? super LT2> c1, final Consumer<? super RT> c2) {

        return (Either3<LT1, LT2, RT>) BiTransformable.super.bipeek(c1, c2);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.functor.BiTransformable#bicast(java.lang.Class,
     * java.lang.Class)
     */
    @Override
    default <U1, U2> Either3<LT1, U1, U2> bicast(final Class<U1> type1, final Class<U2> type2) {

        return (Either3<LT1, U1, U2>) BiTransformable.super.bicast(type1, type2);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.functor.BiTransformable#bitrampoline(java.util.function.Function,
     * java.util.function.Function)
     */
    @Override
    default <R1, R2> Either3<LT1, R1, R2> bitrampoline(
            final Function<? super LT2, ? extends Trampoline<? extends R1>> mapper1,
            final Function<? super RT, ? extends Trampoline<? extends R2>> mapper2) {

        return (Either3<LT1, R1, R2>) BiTransformable.super.bitrampoline(mapper1, mapper2);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> Either3<LT1, LT2, U> cast(final Class<? extends U> type) {

        return (Either3<LT1, LT2, U>) Transformable.super.cast(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Functor#peek(java.util.function.Consumer)
     */
    @Override
    default Either3<LT1, LT2, RT> peek(final Consumer<? super RT> c) {

        return (Either3<LT1, LT2, RT>) Transformable.super.peek(c);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> Either3<LT1, LT2, R> trampoline(final Function<? super RT, ? extends Trampoline<? extends R>> mapper) {

        return (Either3<LT1, LT2, R>) Transformable.super.trampoline(mapper);
    }



    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final static class Lazy<ST, M, PT> implements Either3<ST, M, PT> {

        private final Eval<Either3<ST, M, PT>> lazy;

        public Either3<ST, M, PT> resolve() {
            return lazy.get()
                       .visit(Either3::left1, Either3::left2, Either3::right);
        }

        private static <ST, M, PT> Lazy<ST, M, PT> lazy(final Eval<Either3<ST, M, PT>> lazy) {
            return new Lazy<>(
                              lazy);
        }

        @Override
        public <R> Either3<ST, M, R> map(final Function<? super PT, ? extends R> mapper) {

            return flatMap(t -> Either3.right(mapper.apply(t)));

        }

        @Override
        public <RT1> Either3<ST, M, RT1> flatMap(
                final Function<? super PT, ? extends Either3<ST,M,? extends RT1>> mapper) {

            return Either3.fromLazy(lazy.map(m->m.flatMap(mapper)));


        }
        @Override
        public Trampoline<Either3<ST,M,PT>> toTrampoline() {
            Trampoline<Either3<ST,M,PT>> trampoline = lazy.toTrampoline();
            return new Trampoline<Either3<ST,M,PT>>() {
                @Override
                public Either3<ST,M,PT> get() {
                    Either3<ST,M,PT> either = lazy.get();
                    while (either instanceof Either3.Lazy) {
                        either = ((Either3.Lazy<ST,M,PT>) either).lazy.get();
                    }
                    return either;
                }
                @Override
                public boolean complete(){
                    return false;
                }
                @Override
                public Trampoline<Either3<ST,M,PT>> bounce() {
                    Either3<ST,M,PT> either = lazy.get();
                    if(either instanceof Either3.Lazy){
                        return either.toTrampoline();
                    }
                    return Trampoline.done(either);

                }
            };
        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.fromEval(Eval.later(() -> resolve().filter(test)))
                        .flatMap(Function.identity());

        }

        @Override
        public PT get() {
            return trampoline().get();
        }

        private Either3<ST,M,PT> trampoline(){
            Either3<ST,M,PT> maybe = lazy.get();
            while (maybe instanceof Lazy) {
                maybe = ((Lazy<ST,M,PT>) maybe).lazy.get();
            }
            return maybe;
        }
        @Override
        public ReactiveSeq<PT> stream() {

            return trampoline()
                       .stream();
        }

        @Override
        public Iterator<PT> iterator() {

            return trampoline()
                       .iterator();
        }

        @Override
        public <R> R visit(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {

            return trampoline()
                       .visit(present, absent);
        }

        @Override
        public void subscribe(final Subscriber<? super PT> s) {

            lazy.get()
                .subscribe(s);
        }



        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid, final Function<? super PT, ? extends R> primary) {

            return trampoline()
                       .visit(secondary, mid, primary);
        }

        @Override
        public Either3<ST, PT, M> swap2() {
            return lazy(Eval.later(() -> resolve().swap2()));
        }

        @Override
        public Either3<PT, M, ST> swap1() {
            return lazy(Eval.later(() -> resolve().swap1()));
        }

        @Override
        public boolean isRight() {
            return trampoline()
                       .isRight();
        }

        @Override
        public boolean isLeft1() {
            return trampoline()
                       .isLeft1();
        }

        @Override
        public boolean isLeft2() {
            return trampoline()
                       .isLeft2();
        }

        @Override
        public <R1, R2> Either3<ST, R1, R2> bimap(final Function<? super M, ? extends R1> fn1,
                final Function<? super PT, ? extends R2> fn2) {
            return lazy(Eval.later(() -> resolve().bimap(fn1, fn2)));
        }

        @Override
        public <T> Either3<ST, M, T> unit(final T unit) {

            return Either3.right(unit);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.visit(Either3::left1,Either3::left2,Either3::right).hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
           return this.visit(Either3::left1,Either3::left2,Either3::right).equals(obj);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return trampoline().toString();
        }


        @Override
        public <R> R fold(Function<? super ST, ? extends R> fn1, Function<? super M, ? extends R> fn2, Function<? super PT, ? extends R> fn3) {
            return this.lazy.get().fold(fn1,fn2,fn3);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Right<ST, M, PT> implements Either3<ST, M, PT> {
        private final Eval<PT> value;

        @Override
        public <R> Either3<ST, M, R> map(final Function<? super PT, ? extends R> fn) {
            return new Right<ST, M, R>(
                                       value.map(fn));
        }

        @Override
        public Either3<ST, M, PT> peek(final Consumer<? super PT> action) {
            return map(i -> {
                action.accept(i);
                return i;
            });

        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.fromEval(Eval.later(() -> test.test(get()) ? Maybe.just(get()) : Maybe.<PT> none()))
                        .flatMap(Function.identity());

        }

        @Override
        public PT get() {
            return value.get();
        }

        @Override
        public <RT1> Either3<ST, M, RT1> flatMap(
                final Function<? super PT, ? extends Either3<ST,M,? extends RT1>> mapper) {

            Eval<? extends Either3<? extends ST, ? extends M, ? extends RT1>> et = value.map(mapper);


            final Eval<Either3<ST, M, RT1>> e3 =  (Eval<Either3<ST, M, RT1>>)et;
            return new Lazy<>(
                              e3);


        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public boolean isLeft1() {
            return false;
        }

        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either3.right[" + value.get() + "]";
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid, final Function<? super PT, ? extends R> primary) {
            return primary.apply(value.get());
        }



        @Override
        public <R1, R2> Either3<ST, R1, R2> bimap(final Function<? super M, ? extends R1> fn1,
                final Function<? super PT, ? extends R2> fn2) {
            return (Either3<ST, R1, R2>) this.map(fn2);
        }

        @Override
        public ReactiveSeq<PT> stream() {
            return value.stream();
        }

        @Override
        public Iterator<PT> iterator() {
            return value.iterator();
        }

        @Override
        public <R> R visit(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {
            return value.visit(present, absent);
        }

        @Override
        public void subscribe(final Subscriber<? super PT> s) {
            value.subscribe(s);

        }


        @Override
        public <T> Either3<ST, M, T> unit(final T unit) {
            return Either3.right(unit);
        }

        @Override
        public Either3<ST, PT, M> swap2() {

            return new Left2<>(value);
        }

        @Override
        public Either3<PT, M, ST> swap1() {

            return new Left1<>(
                              value);
        }

        @Override
        public boolean isLeft2() {

            return false;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if(obj instanceof Lazy){
                return ((Lazy)obj).equals(this);
            }
            if (getClass() != obj.getClass())
                return false;
            Right other = (Right) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }


        @Override
        public <R> R fold(Function<? super ST, ? extends R> fn1, Function<? super M, ? extends R> fn2, Function<? super PT, ? extends R> fn3) {
            return fn3.apply(value.get());
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Left1<ST, M, PT> implements Either3<ST, M, PT> {
        private final Eval<ST> value;

        @Override
        public <R> Either3<ST, M, R> map(final Function<? super PT, ? extends R> fn) {
            return (Either3<ST, M, R>) this;
        }

        @Override
        public Either3<ST, M, PT> peek(final Consumer<? super PT> action) {
            return this;

        }
        @Override
        public void subscribe(final Subscriber<? super PT> s) {
            s.onComplete();
        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.none();

        }

        @Override
        public PT get() {
            throw new NoSuchElementException(
                                             "Attempt to access right value on a Left Either3");
        }

        @Override
        public <RT1> Either3<ST, M, RT1> flatMap(
                final Function<? super PT, ? extends Either3<ST,M,? extends RT1>> mapper) {

            return (Either3) this;

        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public boolean isLeft1() {
            return true;
        }

        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either3.left1[" + value.get() + "]";
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid, final Function<? super PT, ? extends R> primary) {
            return secondary.apply(value.get());
        }



        @Override
        public <R1, R2> Either3<ST, R1, R2> bimap(final Function<? super M, ? extends R1> fn1,
                final Function<? super PT, ? extends R2> fn2) {
            return (Either3<ST, R1, R2>) this;
        }

        @Override
        public ReactiveSeq<PT> stream() {
            return ReactiveSeq.empty();
        }

        @Override
        public Iterator<PT> iterator() {
            return Arrays.<PT> asList()
                         .iterator();
        }

        @Override
        public <R> R visit(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {
            return absent.get();
        }


        @Override
        public <T> Either3<ST, M, T> unit(final T unit) {
            return Either3.right(unit);
        }

        @Override
        public Either3<ST, PT, M> swap2() {

            return (Either3<ST, PT, M>) this;
        }

        @Override
        public Either3<PT, M, ST> swap1() {

            return new Right<>(
                               value);
        }

        @Override
        public boolean isLeft2() {

            return false;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if(obj instanceof Lazy){
                return ((Lazy)obj).equals(this);
            }
            if (getClass() != obj.getClass())
                return false;
            Left1 other = (Left1) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

        @Override
        public <R> R fold(Function<? super ST, ? extends R> fn1, Function<? super M, ? extends R> fn2, Function<? super PT, ? extends R> fn3) {
            return fn1.apply(value.get());
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Left2<ST, M, PT> implements Either3<ST, M, PT> {
        private final Eval<M> value;

        @Override
        public <R> Either3<ST, M, R> map(final Function<? super PT, ? extends R> fn) {
            return (Either3<ST, M, R>) this;
        }

        @Override
        public Either3<ST, M, PT> peek(final Consumer<? super PT> action) {
            return this;

        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.none();

        }

        @Override
        public PT get() {
            throw new NoSuchElementException(
                                             "Attempt to access right value on a Middle Either3");
        }

        @Override
        public <RT1> Either3<ST, M, RT1> flatMap(
                final Function<? super PT, ? extends Either3<ST,M,? extends RT1>> mapper) {

            return (Either3) this;

        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public boolean isLeft1() {
            return false;
        }

        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either3.left2[" + value.get() + "]";
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid, final Function<? super PT, ? extends R> primary) {
            return mid.apply(value.get());
        }


        @Override
        public <R1, R2> Either3<ST, R1, R2> bimap(final Function<? super M, ? extends R1> fn1,
                final Function<? super PT, ? extends R2> fn2) {
            return (Either3<ST, R1, R2>) this;
        }

        @Override
        public ReactiveSeq<PT> stream() {
            return ReactiveSeq.empty();
        }

        @Override
        public Iterator<PT> iterator() {
            return Arrays.<PT> asList()
                         .iterator();
        }

        @Override
        public <R> R visit(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {
            return absent.get();
        }

        @Override
        public void subscribe(final Subscriber<? super PT> s) {
                s.onComplete();
        }


        @Override
        public <T> Either3<ST, M, T> unit(final T unit) {
            return Either3.right(unit);
        }

        @Override
        public Either3<ST, PT, M> swap2() {
            return new Right<>(
                               value);

        }

        @Override
        public Either3<PT, M, ST> swap1() {
            return (Either3<PT, M, ST>) this;

        }

        @Override
        public boolean isLeft2() {

            return true;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            if(obj instanceof Lazy){
                return ((Lazy)obj).equals(this);
            }
            Left2 other = (Left2) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

        @Override
        public <R> R fold(Function<? super ST, ? extends R> fn1, Function<? super M, ? extends R> fn2, Function<? super PT, ? extends R> fn3) {
            return fn2.apply(value.get());
        }
    }
    public static <L1,L2,T> Either3<L1,L2,T> narrowK3(final Higher3<either3, L1,L2,T> xor) {
        return (Either3<L1,L2,T>)xor;
    }
    public static <L1,L2,T> Either3<L1,L2,T> narrowK(final Higher<Higher<Higher<either3, L1>,L2>,T> xor) {
        return (Either3<L1,L2,T>)xor;
    }
    default Active<Higher<Higher<either3, LT1>, LT2>,RT> allTypeclasses(){
        return Active.of(this,Instances.definitions());
    }
    default <W2,R> Nested<Higher<Higher<either3, LT1>, LT2>,W2,R> mapM(Function<? super RT,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }
    public static class Instances {

        public static <L1,L2> InstanceDefinitions<Higher<Higher<either3, L1>, L2>> definitions() {
            return new InstanceDefinitions<Higher<Higher<either3, L1>, L2>> () {


                @Override
                public <T, R> Functor<Higher<Higher<either3, L1>, L2>> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<Higher<Higher<either3, L1>, L2>> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<Higher<Higher<either3, L1>, L2>> applicative() {
                    return Instances.applicative();
                }

                @Override
                public <T, R> Monad<Higher<Higher<either3, L1>, L2>> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<Higher<Higher<either3, L1>, L2>>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<Higher<either3, L1>, L2>>> monadPlus() {
                    return Maybe.none();
                }

                @Override
                public <T> MonadRec<Higher<Higher<either3, L1>, L2>> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<Higher<either3, L1>, L2>>> monadPlus(Monoid<Higher<Higher<Higher<either3, L1>, L2>, T>> m) {
                    return Maybe.none();
                }

                @Override
                public <C2, T> Traverse<Higher<Higher<either3, L1>, L2>> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<Higher<Higher<either3, L1>, L2>> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<Higher<Higher<either3, L1>, L2>>> comonad() {
                    return Maybe.just(Instances.comonad());
                }

                @Override
                public <T> Maybe<Unfoldable<Higher<Higher<either3, L1>, L2>>> unfoldable() {
                    return Maybe.none();
                }
            };

        }
        public static <L1,L2> Functor<Higher<Higher<either3, L1>, L2>> functor() {
            return new Functor<Higher<Higher<either3, L1>, L2>>() {

                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<either3, L1>, L2>, T> ds) {
                    return narrowK(ds).map(fn);
                }
            };
        }
        public static <L1,L2> Pure<Higher<Higher<either3, L1>, L2>> unit() {
            return new Pure<Higher<Higher<either3, L1>, L2>>() {

                @Override
                public <T> Higher<Higher<Higher<either3, L1>, L2>, T> unit(T value) {
                    return Either3.right(value);
                }
            };
        }
        public static <L1,L2> Applicative<Higher<Higher<either3, L1>, L2>> applicative() {
            return new Applicative<Higher<Higher<either3, L1>, L2>>() {


                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> ap(Higher<Higher<Higher<either3, L1>, L2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<either3, L1>, L2>, T> apply) {
                    return  narrowK(fn).flatMap(x -> narrowK(apply).map(x));

                }

                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<either3, L1>, L2>, T> ds) {
                    return Instances.<L1,L2>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<Higher<either3, L1>, L2>, T> unit(T value) {
                    return Instances.<L1,L2>unit().unit(value);
                }
            };
        }
        public static <L1,L2> Monad<Higher<Higher<either3, L1>, L2>> monad() {
            return new Monad<Higher<Higher<either3, L1>, L2>>() {


                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> ap(Higher<Higher<Higher<either3, L1>, L2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<either3, L1>, L2>, T> apply) {
                    return Instances.<L1,L2>applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<either3, L1>, L2>, T> ds) {
                    return Instances.<L1,L2>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<Higher<either3, L1>, L2>, T> unit(T value) {
                    return Instances.<L1,L2>unit().unit(value);
                }

                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<either3, L1>, L2>, R>> fn, Higher<Higher<Higher<either3, L1>, L2>, T> ds) {
                    return narrowK(ds).flatMap(fn.andThen(m->narrowK(m)));
                }
            };
        }
        public static <L1,L2,T,R> MonadRec<Higher<Higher<either3, L1>, L2>> monadRec(){

            return new MonadRec<Higher<Higher<either3, L1>, L2>>(){

                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<Higher<either3, L1>, L2>, ? extends Xor<T, R>>> fn) {
                    return narrowK(fn.apply(initial)).flatMap( eval ->
                            eval.visit(s->narrowK(tailRec(s,fn)),p->Either3.right(p)));
                }


            };
        }
        public static <L1,L2> MonadZero<Higher<Higher<either3, L1>, L2>> monadZero() {
            return new MonadZero<Higher<Higher<either3, L1>, L2>>() {


                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> ap(Higher<Higher<Higher<either3, L1>, L2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<either3, L1>, L2>, T> apply) {
                    return Instances.<L1,L2>applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<either3, L1>, L2>, T> ds) {
                    return Instances.<L1,L2>functor().map(fn,ds);
                }

                @Override
                public Higher<Higher<Higher<either3, L1>, L2>, ?> zero() {
                    return Either3.left1(null);
                }

                @Override
                public <T> Higher<Higher<Higher<either3, L1>, L2>, T> unit(T value) {
                    return Instances.<L1,L2>unit().unit(value);
                }

                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<either3, L1>, L2>, R>> fn, Higher<Higher<Higher<either3, L1>, L2>, T> ds) {
                    return Instances.<L1,L2>monad().flatMap(fn,ds);
                }
            };
        }
        public static  <L1,L2> Traverse<Higher<Higher<either3, L1>, L2>> traverse() {
            return new Traverse<Higher<Higher<either3, L1>, L2>> () {


                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> ap(Higher<Higher<Higher<either3, L1>, L2>, ? extends Function<T, R>> fn, Higher<Higher<Higher<either3, L1>, L2>, T> apply) {
                    return Instances.<L1,L2>applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<either3, L1>, L2>, T> ds) {
                    return Instances.<L1,L2>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<Higher<either3, L1>, L2>, T> unit(T value) {
                    return Instances.<L1, L2>unit().unit(value);
                }

                @Override
                public <C2, T, R> Higher<C2, Higher<Higher<Higher<either3, L1>, L2>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<Higher<either3, L1>, L2>, T> ds) {
                    Either3<L1,L2,T> maybe = narrowK(ds);
                    return maybe.visit(left->  applicative.unit(Either3.<L1,L2,R>left1(left)),
                            middle->applicative.unit(Either3.<L1,L2,R>left2(middle)),
                            right->applicative.map(m->Either3.right(m), fn.apply(right)));
                }

                @Override
                public <C2, T> Higher<C2, Higher<Higher<Higher<either3, L1>, L2>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<Higher<either3, L1>, L2>, Higher<C2, T>> ds) {
                    return traverseA(applicative,Function.identity(),ds);
                }


            };
        }
        public static <L1,L2> Foldable<Higher<Higher<either3, L1>, L2>> foldable() {
            return new Foldable<Higher<Higher<either3, L1>, L2>>() {


                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<Higher<either3, L1>, L2>, T> nestedA) {
                    return foldLeft(mb,narrowK(nestedA).<R>map(fn));
                }

                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<Higher<Higher<either3, L1>, L2>, T> ds) {
                    return narrowK(ds).fold(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<Higher<either3, L1>, L2>, T> ds) {
                    return narrowK(ds).fold(monoid);
                }

            };
        }
        public static <L1,L2> Comonad<Higher<Higher<either3, L1>, L2>> comonad() {
            return new ComonadByPure<Higher<Higher<either3, L1>, L2>>() {


                @Override
                public <T> T extract(Higher<Higher<Higher<either3, L1>, L2>, T> ds) {
                    return narrowK(ds).get();
                }

                @Override
                public <T, R> Higher<Higher<Higher<either3, L1>, L2>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<either3, L1>, L2>, T> ds) {
                    return Instances.<L1,L2>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<Higher<either3, L1>, L2>, T> unit(T value) {
                    return Instances.<L1, L2>unit().unit(value);
                }
            };
        }

    }
}
