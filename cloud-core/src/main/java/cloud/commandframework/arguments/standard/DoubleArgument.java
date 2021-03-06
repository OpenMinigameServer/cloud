//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package cloud.commandframework.arguments.standard;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.NumberParseException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class DoubleArgument<C> extends CommandArgument<C, Double> {

    private final double min;
    private final double max;

    private DoubleArgument(
            final boolean required,
            final @NonNull String name,
            final double min,
            final double max,
            final String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider
    ) {
        super(required, name, new DoubleParser<>(min, max), defaultValue, Double.class, suggestionsProvider);
        this.min = min;
        this.max = max;
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Double> of(final @NonNull String name) {
        return DoubleArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Double> optional(final @NonNull String name) {
        return DoubleArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command argument with a default value
     *
     * @param name       Argument name
     * @param defaultNum Default num
     * @param <C>        Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Double> optional(
            final @NonNull String name,
            final double defaultNum
    ) {
        return DoubleArgument.<C>newBuilder(name).asOptionalWithDefault(Double.toString(defaultNum)).build();
    }

    /**
     * Get the minimum accepted double that could have been parsed
     *
     * @return Minimum double
     */
    public double getMin() {
        return this.min;
    }

    /**
     * Get the maximum accepted double that could have been parsed
     *
     * @return Maximum double
     */
    public double getMax() {
        return this.max;
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Double> {

        private double min = Double.NEGATIVE_INFINITY;
        private double max = Double.POSITIVE_INFINITY;

        private Builder(final @NonNull String name) {
            super(Double.class, name);
        }

        /**
         * Set a minimum value
         *
         * @param min Minimum value
         * @return Builder instance
         */
        public @NonNull Builder<C> withMin(final int min) {
            this.min = min;
            return this;
        }

        /**
         * Set a maximum value
         *
         * @param max Maximum value
         * @return Builder instance
         */
        public @NonNull Builder<C> withMax(final int max) {
            this.max = max;
            return this;
        }

        /**
         * Builder a new double argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull DoubleArgument<C> build() {
            return new DoubleArgument<>(this.isRequired(), this.getName(), this.min, this.max,
                    this.getDefaultValue(), this.getSuggestionsProvider()
            );
        }

    }

    public static final class DoubleParser<C> implements ArgumentParser<C, Double> {

        private final double min;
        private final double max;

        /**
         * Construct a new double parser
         *
         * @param min Minimum value
         * @param max Maximum value
         */
        public DoubleParser(final double min, final double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public @NonNull ArgumentParseResult<Double> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        DoubleParser.class,
                        commandContext
                ));
            }
            try {
                final double value = Double.parseDouble(input);
                if (value < this.min || value > this.max) {
                    return ArgumentParseResult.failure(new DoubleParseException(
                            input,
                            this.min,
                            this.max,
                            commandContext
                    ));
                }
                inputQueue.remove();
                return ArgumentParseResult.success(value);
            } catch (final Exception e) {
                return ArgumentParseResult.failure(new DoubleParseException(
                        input,
                        this.min,
                        this.max,
                        commandContext
                ));
            }
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

        /**
         * Get the max value
         *
         * @return Max value
         */
        public double getMax() {
            return this.max;
        }

        /**
         * Get the min value
         *
         * @return Min value
         */
        public double getMin() {
            return this.min;
        }

    }


    public static final class DoubleParseException extends NumberParseException {

        private static final long serialVersionUID = 1764554911581976586L;

        /**
         * Construct a new double parse exception
         *
         * @param input          String input
         * @param min            Minimum value
         * @param max            Maximum value
         * @param commandContext Command context
         */
        public DoubleParseException(
                final @NonNull String input,
                final double min,
                final double max,
                final @NonNull CommandContext<?> commandContext
        ) {
            super(
                    input,
                    min,
                    max,
                    DoubleParser.class,
                    commandContext
            );
        }

        @Override
        public boolean hasMin() {
            return this.getMin().doubleValue() != Double.MIN_VALUE;
        }

        @Override
        public boolean hasMax() {
            return this.getMax().doubleValue() != Double.MAX_VALUE;
        }

        @Override
        public @NonNull String getNumberType() {
            return "double";
        }

    }

}
