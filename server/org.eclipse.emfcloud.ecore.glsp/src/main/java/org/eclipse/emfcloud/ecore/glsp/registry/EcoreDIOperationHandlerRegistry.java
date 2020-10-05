/********************************************************************************
 * Copyright (c) 2019-2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.glsp.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.glsp.api.handler.OperationHandler;
import org.eclipse.glsp.api.operation.Operation;
import org.eclipse.glsp.api.registry.MapRegistry;
import org.eclipse.glsp.api.registry.OperationHandlerRegistry;
import org.eclipse.glsp.api.utils.ReflectionUtil;
import org.eclipse.glsp.server.registry.DIOperationHandlerRegistry;

import com.google.inject.Inject;

/**
 * <p>
 * Temporary workaround to support Ecore CreateOperations, until https://github.com/eclipse-glsp/glsp/issues/21
 * is fixed.
 * </p>
 * <p>
 * The GLSP version {@link DIOperationHandlerRegistry} has special handling for CreateOperations that requires
 * 1 CreateOperationHandler per element type, which doesn't match the current Ecore GLSP structure.
 * </p>
 */
public class EcoreDIOperationHandlerRegistry implements OperationHandlerRegistry {

	private final MapRegistry<String, List<OperationHandler>> internalRegistry;

	@Inject
	public EcoreDIOperationHandlerRegistry(Set<OperationHandler> handlers) {
		internalRegistry = new MapRegistry<>() {
		};
		handlers.forEach(handler -> {
			ReflectionUtil.construct(handler.getHandledOperationType())
					.ifPresent(operation -> register(operation, handler));
		});
	}

	@Override
	public boolean register(final Operation key, final OperationHandler handler) {
		String keyStr = deriveKey(key);
		List<OperationHandler> handlers;
		if (!internalRegistry.hasKey(keyStr)) {
			handlers = new ArrayList<>();
			internalRegistry.register(keyStr, handlers);
		} else {
			Optional<List<OperationHandler>> optional = internalRegistry.get(keyStr);
			if (! optional.isPresent()) {
				return false;
			}
			handlers = optional.get();
		}
		handlers.add(handler);
		return true;
	}

	@Override
	public boolean deregister(final Operation key) {
		return internalRegistry.deregister(deriveKey(key));
	}

	@Override
	public boolean hasKey(final Operation key) {
		return internalRegistry.hasKey(deriveKey(key));
	}

	@Override
	public Optional<OperationHandler> get(final Operation key) {
		return internalRegistry.get(deriveKey(key))
				.flatMap(list -> list.stream().filter(handler -> handler.handles(key)).findFirst());
	}

	@Override
	public Set<OperationHandler> getAll() {
		return internalRegistry.getAll().stream().flatMap(Collection::stream).collect(Collectors.toSet());
	}

	protected String deriveKey(final Operation key) {
		return key.getClass().getName();
	}

	@Override
	public Set<Operation> keys() {
		// TODO Auto-generated method stub
		return null;
	}
}
