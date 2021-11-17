/********************************************************************************
 * Copyright (c) 2019-2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
import "@eclipse-glsp/client/css/tool-palette.css";
import "@eclipse-glsp/theia-integration/css/tool-palette.css";

import { EnableToolPaletteAction } from "@eclipse-glsp/client";
import { ContainerModule } from "inversify";
import { configureActionHandler, EnableDefaultToolsAction, TYPES } from "sprotty";

import { EcoreToolPalette } from "./tool-palette";

const ecoreToolPaletteModule = new ContainerModule((bind, _unbind, isBound) => {
    bind(EcoreToolPalette).toSelf().inSingletonScope();
    bind(TYPES.IUIExtension).toService(EcoreToolPalette);
    configureActionHandler({ bind, isBound }, EnableToolPaletteAction.KIND, EcoreToolPalette);
    configureActionHandler({ bind, isBound }, EnableDefaultToolsAction.KIND, EcoreToolPalette);
});

export default ecoreToolPaletteModule;
