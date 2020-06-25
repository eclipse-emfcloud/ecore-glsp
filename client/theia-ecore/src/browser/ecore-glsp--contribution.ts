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
import { BaseGLSPClientContribution } from "@eclipse-glsp/theia-integration/lib/browser";
import { injectable } from "inversify";

import { EcoreLanguage } from "../common/ecore-language";

@injectable()
export class EcoreGLSPClientContribution extends BaseGLSPClientContribution {
    fileExtensions = [EcoreLanguage.FileExtension];
    readonly id = EcoreLanguage.Id;
    readonly name = EcoreLanguage.Name;

}
