/********************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
import "../../style/jsonforms-styling.css";

import { PropertyDataService } from "@theia/property-view/lib/browser/property-data-service";
import { PropertyViewWidgetProvider } from "@theia/property-view/lib/browser/property-view-widget-provider";
import { ContainerModule } from "inversify";

import { EcoreGlspPropertyDataService } from "./property-data-service";
import { EcoreGlspPropertyViewWidgetProvider } from "./widget-provider";

export default new ContainerModule(bind => {
    bind(PropertyDataService).to(EcoreGlspPropertyDataService).inSingletonScope();
    bind(PropertyViewWidgetProvider).to(EcoreGlspPropertyViewWidgetProvider).inSingletonScope();
});
