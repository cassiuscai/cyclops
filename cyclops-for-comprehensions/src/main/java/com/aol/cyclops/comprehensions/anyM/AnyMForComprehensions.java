package com.aol.cyclops.comprehensions.anyM;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.comprehensions.donotation.typed.Do;
import com.aol.cyclops.lambda.monads.AnyMForComprehensionHandler;
import com.aol.cyclops.monad.AnyM;

public class AnyMForComprehensions<U> implements AnyMForComprehensionHandler<U> {

	/* (non-Javadoc)
	 * @see com.aol.cyclops.comprehensions.anyM.AnyMForEach#forEach2(com.aol.cyclops.monad.AnyM, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	public <R1, R> AnyM<R> forEach2(AnyM<U> anyM, Function<U, ? extends AnyM<R1>> monad, Function<U, Function<R1, R>> yieldingFunction) {
		return Do.add(anyM).withAnyM(u -> monad.apply(u)).yield(yieldingFunction).unwrap();

	}

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.comprehensions.anyM.AnyMForEach#forEach2(com.aol.cyclops.monad.AnyM, java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	public <R1,R> AnyM<R> forEach2(AnyM<U> anyM, Function<U,? extends AnyM<R1>> monad, 
												Function<U, Function<R1, Boolean>> filterFunction,
													Function<U,Function<R1,R>> yieldingFunction ){
		 return Do.add(anyM)
				  .withAnyM(u->monad.apply(u))
				  .filter(filterFunction)
				  .yield(yieldingFunction).unwrap();
			
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.comprehensions.anyM.AnyMForEach#forEach3(com.aol.cyclops.monad.AnyM, java.util.function.Function, java.util.function.BiFunction, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	public <R1,R2,R> AnyM<R> forEach3(AnyM<U> anyM, Function<U,? extends AnyM<R1>> monad1, 
			BiFunction<U,R1,? extends AnyM<R2>> monad2,
					Function<U,Function<R1,Function<R2,Boolean>>> filterFunction,
			Function<U,Function<R1,Function<R2,R>>> yieldingFunction ){
		return Do.add(anyM)
				  .withAnyM(u->monad1.apply(u))
				  .withAnyM(u1->u2->monad2.apply(u1,u2))
				  .filter(filterFunction)
				  .yield(yieldingFunction).unwrap();
		
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.comprehensions.anyM.AnyMForEach#forEach3(com.aol.cyclops.monad.AnyM, java.util.function.Function, java.util.function.BiFunction, java.util.function.Function)
	 */
	@Override
	public <R1,R2,R> AnyM<R> forEach3(AnyM<U> anyM,Function<U,? extends AnyM<R1>> monad1, 
			BiFunction<U,R1,? extends AnyM<R2>> monad2,
			Function<U,Function<R1,Function<R2,R>>> yieldingFunction ){
		return Do.add(anyM)
				  .withAnyM(u->monad1.apply(u))
				  .withAnyM(u1->u2->monad2.apply(u1,u2))
				  .yield(yieldingFunction).unwrap();
	}

}
