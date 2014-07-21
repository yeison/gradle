/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.artifacts.component.replacement;

import groovy.lang.Closure;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.internal.ClosureSpec;

import java.util.LinkedList;
import java.util.List;

public class ComponentReplacements implements ComponentReplacementRules {

    private final List<DefaultComponentReplacementTarget> targets = new LinkedList<DefaultComponentReplacementTarget>();

    public ComponentReplacementTarget from(Object from) {
        return new DefaultComponentReplacementTarget(from);
    }

    public Spec<ModuleIdentifier> getReplacementSourceSelector(final ModuleIdentifier replaceTarget) {
        return new Spec<ModuleIdentifier>() {
            public boolean isSatisfiedBy(ModuleIdentifier element) {
                for (DefaultComponentReplacementTarget target : targets) {
                    if (target.into.isSatisfiedBy(replaceTarget)) {
                        return target.from.isSatisfiedBy(element);
                    }
                }
                return false;
            }
        };
    }

    public Spec<ModuleIdentifier> getReplacementTargetSelector(final ModuleIdentifier replaceSource) {
        return new Spec<ModuleIdentifier>() {
            public boolean isSatisfiedBy(ModuleIdentifier element) {
                for (DefaultComponentReplacementTarget target : targets) {
                    if (target.from.isSatisfiedBy(replaceSource)) {
                        return target.into.isSatisfiedBy(element);
                    }
                }
                return false;
            }
        };
    }

    private class DefaultComponentReplacementTarget implements ComponentReplacementTarget {

        private final Spec<ModuleIdentifier> from;
        private Spec<ModuleIdentifier> into;

        public DefaultComponentReplacementTarget(Object fromObject) {
            this.from = convertInput(fromObject);
        }

        public void into(final Object intoObject) {
            into = convertInput(intoObject);
            targets.add(this);
        }

        private Spec<ModuleIdentifier> convertInput(Object input) {
            Spec<ModuleIdentifier> result;
            if (input instanceof String) {
                final String[] split = ((String) input).split(":");
                result = new Spec<ModuleIdentifier>() {
                    public boolean isSatisfiedBy(ModuleIdentifier element) {
                        return element.getGroup().equals(split[0]) && element.getName().equals(split[1]);
                    }
                };
            } else if (input instanceof Closure) {
                result = new ClosureSpec((Closure) input);
            } else {
                throw new InvalidUserDataException("Don't know how to use provided component replacement value: " + input);
            }
            return result;
        }
    }
}
