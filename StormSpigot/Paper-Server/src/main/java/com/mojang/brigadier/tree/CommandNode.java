package com.mojang.brigadier.tree;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.AmbiguityConsumer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.server.CommandListenerWrapper; // CraftBukkit

public abstract class CommandNode<S> implements Comparable<CommandNode<S>> {
    private Map<String, CommandNode<S>> children = Maps.newTreeMap(); //Paper - Switch to tree map for automatic sorting
    private Map<String, LiteralCommandNode<S>> literals = Maps.newLinkedHashMap();
    private Map<String, ArgumentCommandNode<S, ?>> arguments = Maps.newLinkedHashMap();
    private final Predicate<S> requirement;
    private final CommandNode<S> redirect;
    private final RedirectModifier<S> modifier;
    private final boolean forks;
    private Command<S> command;
    // CraftBukkit start
    public void removeCommand(String name) {
        children.remove(name);
        literals.remove(name);
        arguments.remove(name);
    }
    // CraftBukkit end

    protected CommandNode(final Command<S> command, final Predicate<S> requirement, final CommandNode<S> redirect, final RedirectModifier<S> modifier, final boolean forks) {
        this.command = command;
        this.requirement = requirement;
        this.redirect = redirect;
        this.modifier = modifier;
        this.forks = forks;
    }

    public Command<S> getCommand() {
        return command;
    }

    public Collection<CommandNode<S>> getChildren() {
        return children.values();
    }

    public CommandNode<S> getChild(final String name) {
        return children.get(name);
    }

    public CommandNode<S> getRedirect() {
        return redirect;
    }

    public RedirectModifier<S> getRedirectModifier() {
        return modifier;
    }

    public boolean canUse(final S source) {
        // CraftBukkit start
        if (source instanceof CommandListenerWrapper) {
            try {
                ((CommandListenerWrapper) source).currentCommand = this;
                return requirement.test(source);
            } finally {
                ((CommandListenerWrapper) source).currentCommand = null;
            }
        }
        // CraftBukkit end
        return requirement.test(source);
    }

    public void addChild(final CommandNode<S> node) {
        if (node instanceof RootCommandNode) {
            throw new UnsupportedOperationException("Cannot add a RootCommandNode as a child to any other CommandNode");
        }

        final CommandNode<S> child = children.get(node.getName());
        if (child != null) {
            // We've found something to merge onto
            if (node.getCommand() != null) {
                child.command = node.getCommand();
            }
            for (final CommandNode<S> grandchild : node.getChildren()) {
                child.addChild(grandchild);
            }
        } else {
            children.put(node.getName(), node);
            if (node instanceof LiteralCommandNode) {
                literals.put(node.getName(), (LiteralCommandNode<S>) node);
            } else if (node instanceof ArgumentCommandNode) {
                arguments.put(node.getName(), (ArgumentCommandNode<S, ?>) node);
            }
        }
        //Paper - Remove manual sorting, it is no longer needed
    }

    public void findAmbiguities(final AmbiguityConsumer<S> consumer) {
        Set<String> matches = Sets.newHashSet();

        for (final CommandNode<S> child : children.values()) {
            for (final CommandNode<S> sibling : children.values()) {
                if (child == sibling) {
                    continue;
                }

                for (final String input : child.getExamples()) {
                    if (sibling.isValidInput(input)) {
                        matches.add(input);
                    }
                }

                if (matches.size() > 0) {
                    consumer.ambiguous(this, child, sibling, matches);
                    matches = Sets.newHashSet();
                }
            }

            child.findAmbiguities(consumer);
        }
    }

    protected abstract boolean isValidInput(final String input);

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandNode)) return false;

        final CommandNode<S> that = (CommandNode<S>) o;

        if (!children.equals(that.children)) return false;
        if (command != null ? !command.equals(that.command) : that.command != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return 31 * children.hashCode() + (command != null ? command.hashCode() : 0);
    }

    public Predicate<S> getRequirement() {
        return requirement;
    }

    public abstract String getName();

    public abstract String getUsageText();

    public abstract void parse(StringReader reader, CommandContextBuilder<S> contextBuilder) throws CommandSyntaxException;

    public abstract CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) throws CommandSyntaxException;

    public abstract ArgumentBuilder<S, ?> createBuilder();

    protected abstract String getSortedKey();

    public Collection<? extends CommandNode<S>> getRelevantNodes(final StringReader input) {
        if (literals.size() > 0) {
            final int cursor = input.getCursor();
            while (input.canRead() && input.peek() != ' ') {
                input.skip();
            }
            final String text = input.getString().substring(cursor, input.getCursor());
            input.setCursor(cursor);
            final LiteralCommandNode<S> literal = literals.get(text);
            if (literal != null) {
                return Collections.singleton(literal);
            } else {
                return arguments.values();
            }
        } else {
            return arguments.values();
        }
    }

    @Override
    public int compareTo(final CommandNode<S> o) {
        return ComparisonChain
            .start()
            .compareTrueFirst(this instanceof LiteralCommandNode, o instanceof LiteralCommandNode)
            .compare(getSortedKey(), o.getSortedKey())
            .result();
    }

    public boolean isFork() {
        return forks;
    }

    public abstract Collection<String> getExamples();
}
