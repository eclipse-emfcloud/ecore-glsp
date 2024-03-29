/********************************************************************************
 * Copyright (c) 2019 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
import { compare, createToolGroup, PaletteItem, ToolPalette } from "@eclipse-glsp/client";
import { injectable } from "inversify";

@injectable()
export class EcoreToolPalette extends ToolPalette {

    protected createBody(): void {
        const bodyDiv = document.createElement("div");
        bodyDiv.classList.add("palette-body");
        let tabIndex = 0;
        this.paletteItems.sort(compare)
            .forEach(item => {
                if (item.children) {
                    const group = createToolGroup(item);
                    item.children.sort(compare).forEach(child => group.appendChild(this.createEcoreToolButton(child, tabIndex++, child.icon || "")));
                    bodyDiv.appendChild(group);
                } else {
                    bodyDiv.appendChild(this.createEcoreToolButton(item, tabIndex++, item.icon || "eclass"));
                }
            });
        if (this.paletteItems.length === 0) {
            const noResultsDiv = document.createElement("div");
            noResultsDiv.innerText = "No results found.";
            noResultsDiv.classList.add("tool-button");
            bodyDiv.appendChild(noResultsDiv);
        }
        // Remove existing body to refresh filtered entries
        if (this.bodyDiv) {
            this.containerElement.removeChild(this.bodyDiv);
        }
        this.containerElement.appendChild(bodyDiv);
        this.bodyDiv = bodyDiv;
    }

    protected createEcoreToolButton(item: PaletteItem, index: number, icon: string): HTMLElement {
        const button = document.createElement("div");
        button.appendChild(this.createEcoreIcon(icon));
        button.tabIndex = index;
        button.classList.add("tool-button");
        button.classList.add("ecore-tool-button");
        button.insertAdjacentText("beforeend", item.label);
        button.onclick = super.onClickCreateToolButton(button, item);
        button.onkeydown = ev => this.clearToolOnEscape(ev);
        return button;
    }

    protected createEcoreIcon(cssClass: string): HTMLDivElement {
        const icon = document.createElement("div");
        icon.classList.add(...["ecoreimg", cssClass]);
        return icon;
    }

}
