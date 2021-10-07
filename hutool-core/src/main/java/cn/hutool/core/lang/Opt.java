/*
 * Copyright (c) 2012, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package cn.hutool.core.lang;

import cn.hutool.core.lang.func.VoidFunc0;
import cn.hutool.core.util.StrUtil;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 复制jdk16中的Optional，以及自己进行了一点调整和新增，比jdk8中的Optional多了几个实用的函数<br>
 * 详细见：https://gitee.com/dromara/hutool/pulls/426
 *
 * @author VampireAchao
 * @see java.util.Optional
 */
public class Opt<T> {
	/**
	 * 一个空的{@code Opt}
	 */
	private static final Opt<?> EMPTY = new Opt<>(null);

	/**
	 * 包裹里实际的元素
	 */
	private final T value;

	/**
	 * {@code Opt}的构造函数
	 *
	 * @param value 包裹里的元素
	 */
	private Opt(T value) {
		this.value = value;
	}

	/**
	 * 返回一个空的{@code Opt}
	 */
	public static <T> Opt<T> empty() {
		@SuppressWarnings("unchecked")
		Opt<T> t = (Opt<T>) EMPTY;
		return t;
	}

	/**
	 * 返回一个包裹里元素不可能为空的{@code Opt}
	 *
	 * @param value 包裹里的元素
	 * @param <T>   包裹里元素的类型
	 * @return 一个包裹里元素不可能为空的 {@code Opt}
	 * @throws NullPointerException 如果传入的元素为空，抛出 {@code NPE}
	 */
	public static <T> Opt<T> of(T value) {
		return new Opt<>(Objects.requireNonNull(value));
	}

	/**
	 * 返回一个包裹里元素可能为空的{@code Opt}
	 *
	 * @param value 传入需要包裹的元素
	 * @param <T>   包裹里元素的类型
	 * @return 一个包裹里元素可能为空的 {@code Opt}
	 */
	public static <T> Opt<T> ofNullable(T value) {
		return value == null ? empty()
				: new Opt<>(value);
	}

	/**
	 * 返回一个包裹里元素可能为空的{@code Opt}，额外判断了空字符串的情况
	 *
	 * @param value 传入需要包裹的元素
	 * @param <T>   包裹里元素的类型
	 * @return 一个包裹里元素可能为空，或者为空字符串的 {@code Opt}
	 */
	public static <T> Opt<T> ofBlankAble(T value) {
		return StrUtil.isBlankIfStr(value) ? empty() : new Opt<>(value);
	}

	/**
	 * 返回包裹里的元素，取不到则为{@code null}，注意！！！此处和{@link java.util.Optional#get()}不同的一点是本方法并不会抛出{@code NoSuchElementException}
	 * 如果元素为空，则返回{@code null}，如果需要一个绝对不能为{@code null}的值，则使用{@link #orElseThrow()}
	 *
	 * @return 包裹里的元素，有可能为{@code null}
	 * @apiNote 如果需要一个绝对不能为 {@code null}的值，则使用{@link #orElseThrow()}
	 * 做此处修改的原因是，有时候我们确实需要返回一个null给前端，并且这样的时候并不少见
	 * 而使用 {@code .orElse(null)}需要写整整12个字符，用{@code .get()}就只需要6个啦
	 */
	public T get() {
		return value;
	}

	/**
	 * 判断包裹里元素的值是否存在，存在为 {@code true}，否则为{@code false}
	 *
	 * @return 包裹里元素的值存在为 {@code true}，否则为{@code false}
	 */
	public boolean isPresent() {
		return value != null;
	}

	/**
	 * 判断包裹里元素的值是否不存在，不存在为 {@code true}，否则为{@code false}
	 *
	 * @return 包裹里元素的值不存在 则为 {@code true}，否则为{@code false}
	 * @since 11 这是jdk11{@link java.util.Optional}中的新函数
	 */
	public boolean isEmpty() {
		return value == null;
	}

	/**
	 * 如果包裹里的值存在，就执行传入的操作({@link Consumer#accept})
	 *
	 * @param action 你想要执行的操作
	 * @throws NullPointerException 如果包裹里的值存在，但你传入的操作为{@code null}时抛出
	 * @apiNote 例如如果值存在就打印结果
	 * <pre>{@code
	 * Opt.ofNullable("Hello Hutool!").ifPresent(Console::log);
	 * }</pre>
	 */
	public void ifPresent(Consumer<? super T> action) {
		if (value != null) {
			action.accept(value);
		}
	}

	/**
	 * 如果包裹里的值存在，就执行传入的值存在时的操作({@link Consumer#accept})
	 * 否则执行传入的值不存在时的操作({@link VoidFunc0}中的{@link VoidFunc0#call()})
	 *
	 * @param action      包裹里的值存在时的操作
	 * @param emptyAction 包裹里的值不存在时的操作
	 * @throws NullPointerException 如果包裹里的值存在时，执行的操作为 {@code null}, 或者包裹里的值不存在时的操作为 {@code null}，则抛出{@code NPE}
	 * @apiNote 例如值存在就打印对应的值，不存在则用{@code Console.error}打印另一句字符串
	 * <pre>{@code
	 * Opt.ofNullable("Hello Hutool!").ifPresentOrElse(Console::log, () -> Console.error("Ops!Something is wrong!"));
	 * }</pre>
	 * @since 9 这是jdk9{@link java.util.Optional}中的新函数
	 */
	public void ifPresentOrElse(Consumer<? super T> action, VoidFunc0 emptyAction) {
		if (value != null) {
			action.accept(value);
		} else {
			emptyAction.callWithRuntimeException();
		}
	}

	/**
	 * 判断包裹里的值存在并且与给定的条件是否满足 ({@link Predicate#test}执行结果是否为true)
	 * 如果满足条件则返回本身
	 * 不满足条件或者元素本身为空时返回一个返回一个空的{@code Opt}
	 *
	 * @param predicate 给定的条件
	 * @return 如果满足条件则返回本身, 不满足条件或者元素本身为空时返回一个返回一个空的{@code Opt}
	 * @throws NullPointerException 如果给定的条件为 {@code null}，抛出{@code NPE}
	 */
	public Opt<T> filter(Predicate<? super T> predicate) {
		Objects.requireNonNull(predicate);
		if (isEmpty()) {
			return this;
		} else {
			return predicate.test(value) ? this : empty();
		}
	}

	/**
	 * 如果包裹里的值存在，就执行传入的操作({@link Function#apply})并返回一个包裹了该操作返回值的{@code Opt}
	 * 如果不存在，返回一个空的{@code Opt}
	 *
	 * @param mapper 值存在时执行的操作
	 * @param <U>    操作返回值的类型
	 * @return 如果包裹里的值存在，就执行传入的操作({@link Function#apply})并返回一个包裹了该操作返回值的{@code Opt}，
	 * 如果不存在，返回一个空的{@code Opt}
	 * @throws NullPointerException 如果给定的操作为 {@code null}，抛出 {@code NPE}
	 */
	public <U> Opt<U> map(Function<? super T, ? extends U> mapper) {
		Objects.requireNonNull(mapper);
		if (isEmpty()) {
			return empty();
		} else {
			return Opt.ofNullable(mapper.apply(value));
		}
	}

	/**
	 * 如果包裹里的值存在，就执行传入的操作({@link Function#apply})并返回该操作返回值
	 * 如果不存在，返回一个空的{@code Opt}
	 * 和 {@link Opt#map}的区别为 传入的操作返回值必须为 Opt
	 *
	 * @param mapper 值存在时执行的操作
	 * @param <U>    操作返回值的类型
	 * @return 如果包裹里的值存在，就执行传入的操作({@link Function#apply})并返回该操作返回值
	 * 如果不存在，返回一个空的{@code Opt}
	 * @throws NullPointerException 如果给定的操作为 {@code null}或者给定的操作执行结果为 {@code null}，抛出 {@code NPE}
	 */
	public <U> Opt<U> flatMap(Function<? super T, ? extends Opt<? extends U>> mapper) {
		Objects.requireNonNull(mapper);
		if (isEmpty()) {
			return empty();
		} else {
			@SuppressWarnings("unchecked")
			Opt<U> r = (Opt<U>) mapper.apply(value);
			return Objects.requireNonNull(r);
		}
	}

	/**
	 * 如果包裹里元素的值存在，就执行对应的操作，并返回本身
	 * 如果不存在，返回一个空的{@code Opt}
	 *
	 * @param action 值存在时执行的操作
	 * @throws NullPointerException 如果值存在，并且传入的操作为 {@code null}
	 * @apiNote 属于 {@link #ifPresent}的链式拓展
	 * @author VampireAchao
	 */
	public Opt<T> peek(Consumer<T> action) {
		Objects.requireNonNull(action);
		if (isEmpty()) {
			return Opt.empty();
		}
		action.accept(value);
		return this;
	}

	/**
	 * 如果包裹里元素的值存在，就返回本身，如果不存在，则使用传入的操作执行后获得的 {@code Opt}
	 *
	 * @return 如果包裹里元素的值存在，就返回本身，如果不存在，则使用传入的函数执行后获得的 {@code Opt}
	 * @throws NullPointerException 如果传入的操作为空，或者传入的操作执行后返回值为空，则抛出 {@code NPE}
	 * @since 9 这是jdk9{@link java.util.Optional}中的新函数
	 */
	public Opt<T> or(Supplier<? extends Opt<? extends T>> supplier) {
		Objects.requireNonNull(supplier);
		if (isPresent()) {
			return this;
		} else {
			@SuppressWarnings("unchecked")
			Opt<T> r = (Opt<T>) supplier.get();
			return Objects.requireNonNull(r);
		}
	}

	/**
	 * 如果包裹里元素的值存在，就返回一个包含该元素的 {@link Stream},
	 * 否则返回一个空元素的 {@link Stream}
	 *
	 * @return 返回一个包含该元素的 {@link Stream}或空的 {@link Stream}
	 * @apiNote 该方法能将 Opt 中的元素传递给 {@link Stream}
	 * <pre>{@code
	 *     Stream<Opt<T>> os = ..
	 *     Stream<T> s = os.flatMap(Opt::stream)
	 * }</pre>
	 * @since 9 这是jdk9{@link java.util.Optional}中的新函数
	 */
	public Stream<T> stream() {
		if (isEmpty()) {
			return Stream.empty();
		} else {
			return Stream.of(value);
		}
	}

	/**
	 * 如果包裹里元素的值存在，则返回该值，否则返回传入的{@code other}
	 *
	 * @param other 元素为空时返回的值，有可能为 {@code null}.
	 * @return 如果包裹里元素的值存在，则返回该值，否则返回传入的{@code other}
	 */
	public T orElse(T other) {
		return value != null ? value : other;
	}

	/**
	 * 如果包裹里元素的值存在，则返回该值，否则返回传入的操作执行后的返回值
	 *
	 * @param supplier 值不存在时需要执行的操作，返回一个类型与 包裹里元素类型 相同的元素
	 * @return 如果包裹里元素的值存在，则返回该值，否则返回传入的操作执行后的返回值
	 * @throws NullPointerException 如果之不存在，并且传入的操作为空，则抛出 {@code NPE}
	 */
	public T orElseGet(Supplier<? extends T> supplier) {
		return value != null ? value : supplier.get();
	}

	/**
	 * 如果包裹里的值存在，则返回该值，否则抛出 {@code NoSuchElementException}
	 *
	 * @return 返回一个不为 {@code null} 的包裹里的值
	 * @throws NoSuchElementException 如果包裹里的值不存在则抛出该异常
	 * @since 10 这是jdk10{@link java.util.Optional}中的新函数
	 */
	public T orElseThrow() {
		if (value == null) {
			throw new NoSuchElementException("No value present");
		}
		return value;
	}

	/**
	 * 如果包裹里的值存在，则返回该值，否则执行传入的操作，获取异常类型的返回值并抛出
	 *
	 * @param <X>               异常类型
	 * @param exceptionSupplier 值不存在时执行的操作，返回值继承 {@link Throwable}
	 * @return 包裹里不能为空的值
	 * @throws X                    如果值不存在
	 * @throws NullPointerException 如果值不存在并且 传入的操作为 {@code null}或者操作执行后的返回值为{@code null}
	 * @apiNote 往往是一个包含无参构造器的异常 例如传入{@code IllegalStateException::new}
	 */
	public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (value != null) {
			return value;
		} else {
			throw exceptionSupplier.get();
		}
	}

	/**
	 * 如果包裹里的值存在，则返回该值，否则执行传入的操作，获取异常类型的返回值并抛出
	 *
	 * @param <X>               异常类型
	 * @param exceptionFunction 值不存在时执行的操作，返回值继承 {@link Throwable}
	 * @param message           作为传入操作执行时的参数，一般作为异常自定义提示语
	 * @return 包裹里不能为空的值
	 * @throws X                    如果值不存在
	 * @throws NullPointerException 如果值不存在并且 传入的操作为 {@code null}或者操作执行后的返回值为{@code null}
	 * @apiNote 往往是一个包含 自定义消息 构造器的异常 例如
	 * <pre>{@code
	 * 		Opt.ofNullable(null).orElseThrow(IllegalStateException::new, "Ops!Something is wrong!");
	 * }</pre>
	 * @author VampireAchao
	 */
	public <X extends Throwable> T orElseThrow(Function<String, ? extends X> exceptionFunction, String message) throws X {
		if (value != null) {
			return value;
		} else {
			throw exceptionFunction.apply(message);
		}
	}

	/**
	 * 判断传入参数是否与 {@code Opt}相等
	 * 在以下情况下返回true
	 * <ul>
	 * <li>它也是一个 {@code Opt} 并且
	 * <li>它们包裹住的元素都为空 或者
	 * <li>它们包裹住的元素之间相互 {@code equals()}
	 * </ul>
	 *
	 * @param obj 一个要用来判断是否相等的参数
	 * @return 如果传入的参数也是一个 {@code Opt}并且它们包裹住的元素都为空
	 * 或者它们包裹住的元素之间相互 {@code equals()} 就返回{@code true}
	 * 否则返回 {@code false}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Opt)) {
			return false;
		}

		Opt<?> other = (Opt<?>) obj;
		return Objects.equals(value, other.value);
	}

	/**
	 * 如果包裹内元素为空，则返回0，否则返回元素的 {@code hashcode}
	 *
	 * @return 如果包裹内元素为空，则返回0，否则返回元素的 {@code hashcode}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}

	/**
	 * 返回包裹内元素调用{@code toString()}的结果，不存在则返回{@code null}
	 * return 包裹内元素调用{@code toString()}的结果，不存在则返回{@code null}
	 */
	@Override
	public String toString() {
		return value != null
				? value.toString()
				: null;
	}
}
