/********************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
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
