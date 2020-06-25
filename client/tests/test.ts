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
import { join, resolve } from "path";
import { Selector } from "testcafe";

import * as config from "./config.json";

// Converts Windows paths to all / for integration with theia
const relPathToWorkspace = resolve(join(__dirname, '..', 'workspace')).replace(/\\/g, "/");


class Helper {
    static load = async t => {
        await t
            .wait(5000);
        await Selector('.p-MenuBar-content', { visibilityCheck: true });
    }

    static emptyEcore = () => {
        return Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('empty.ecore');
    }

    static glspGraphEcore = () => {
        return Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('glsp-graph.ecore');
    }

    static wsSelect = () => {
        return Selector('#shell-tab-explorer-view-container');
    }
}

class CreateHelper {

    constructor(private t: TestController) {
    }
    async createNode(name) {
        const selector = Selector('div.tool-group > div.tool-button').withText(name);
        const svgCanvas = Selector('svg.sprotty-graph');
        await this.t.click(selector)
            .click(svgCanvas);
    }

    async createClass() {
        return this.createNode('Class');
    }

    async createAbstract() {
        return this.createNode('Abstract');
    }

    async createInterface() {
        return this.createNode('Interface');
    }

    async createEnum() {
        return this.createNode('Enum');
    }

    async createAllNodeTypes() {
        const emptyEcore = Helper.emptyEcore();
        const wsSelect = Helper.wsSelect();

        await this.t
            .click(wsSelect)
            .click(emptyEcore);
        await this.createClass();
        await this.createAbstract();
        await this.createEnum();
        await this.createInterface();
    }

    async createEdge(name, a, b) {
        const selector = Selector('div.tool-group > div.tool-button').withText(name);
        await this.t.click(selector).click(a).hover(b).click(b);
    }

    async layout() {
        await this.t.pressKey('alt+l').wait(500);
    }
    async createAllEdges() {
        await this.layout();
        await this.createEdge('Reference', this.nodesSelector.classNode, this.nodesSelector.abstractNode);
        await this.createEdge('Inheritance', this.nodesSelector.interfaceNode, this.nodesSelector.abstractNode);
        await this.createEdge('Containment', this.nodesSelector.interfaceNode, this.nodesSelector.classNode);
    }

    async deleteAllNodes(t) {
        await t
            .click(this.nodesSelector.classNode)
            .pressKey('delete')
            .click(this.nodesSelector.abstractNode)
            .pressKey('delete')
            .click(this.nodesSelector.enumNode)
            .pressKey('delete')
            .click(this.nodesSelector.interfaceNode)
            .pressKey('delete');
    }

    async addAttributes() {
        const attribute = Selector('div.tool-group > div.tool-button').withText("Attribute");
        const literal = Selector('div.tool-group > div.tool-button').withText("Literal");

        this.layout();

        await this.t
            .click(attribute)
            .click(this.nodesSelector.classNode)
            .click(attribute)
            .click(this.nodesSelector.abstractNode)
            .click(attribute)
            .click(this.nodesSelector.interfaceNode)
            .click(literal)
            .click(this.nodesSelector.enumNode);
    }

    public nodesSelector = {
        classNode: Selector('g.node.ecore-node text.name.sprotty-label').withText('NewEClass0'),
        abstractNode: Selector('g.node.ecore-node.abstract text.name.sprotty-label').withText('NewEClass1'),
        enumNode: Selector('g.node.ecore-node text.name.sprotty-label').withText('NewEEnum2'),
        interfaceNode: Selector('g.node.ecore-node.interface text.name.sprotty-label').withText('NewEClass3')
    };

    public edgeSelector = {
        inheritanceEdge: Selector('g.sprotty-edge.ecore-edge.inheritance'),
        containmentEdge: Selector('g.sprotty-edge.ecore-edge.composition text.edge-name.sprotty-label').withText('neweclass0s'),
        referenceEdge: Selector('g.sprotty-edge.ecore-edge text.edge-name.sprotty-label').withText('neweclass1s')
    };

    async getEdgePosition(s: Selector) {
        const x = await (s.getAttribute('cx'));
        const y = await (s.getAttribute('cy'));
        return { x: parseFloat(x), y: parseFloat(y) };
    }

    async getNodePosition(s: Selector) {
        const transform = await (s.parent("g.node.ecore-node").getAttribute('transform'));
        const regex = new RegExp("\\s*\\w*\\s*\\(\\s*(\\d+(\\.\\d+)?)\\s*,\\s*(\\d+(\\.\\d+)?)\\s*\\)\\s*");
        const match = regex.exec(transform);
        if (match) {
            return { x: parseFloat(match[1]), y: parseFloat(match[3]) };
        }
        return {};
    }
}

const checkDefaultWorkbench = async (t) => {
    const emptyEcore = Helper.emptyEcore();
    const glspGraphEcore = Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('glsp-graph.ecore');
    const glspGraphEnotation = Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('glsp-graph.enotation');
    const umlEcore = Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('UML.ecore');
    await t
        .expect(emptyEcore.exists).ok('Check if empty.ecore exists')
        .expect(glspGraphEcore.exists).ok('Check if glsp-graph.ecore exists')
        .expect(glspGraphEnotation.exists).ok('Check if glsp-graph.enotation exists')
        .expect(umlEcore.exists).ok('Check if UML.ecore exists')
        .expect(Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').count).eql(4);
};

const openQuickAccessBar = async (t) => {
    const viewMenu = Selector('.p-MenuBar-item').withText('View');
    const findCommand = Selector('.p-Menu-itemLabel').withText('Find Command...');

    await t
        .click(viewMenu)
        .click(findCommand);
};

const writeQuickAccessBar = async (t, text) => {
    const quickAccess = Selector('div.monaco-inputbox.idle input.input');

    await t
        .typeText(quickAccess, text)
        .pressKey('Enter');
};

const port = process.env.PORT || config.defaultPort;

fixture`Ecore-glsp E2E-Testing`// declare the fixture
    .page`http://localhost:${port}/#${relPathToWorkspace}`
    .beforeEach(async t => {
        await Helper.load(t);
    });  // The start page loads the workbench at the above defined Path

test('Open Workbench', async t => {
    const workspace = Helper.wsSelect();
    await t
        .wait(5000)
        .click(workspace);
    await checkDefaultWorkbench(t);
});

test('Switch Theme', async t => {

    openQuickAccessBar(t);
    writeQuickAccessBar(t, "Change Color Theme");
    writeQuickAccessBar(t, "Dark");

    await t
        .expect(Selector('div.p-Widget.p-DockPanel.p-SplitPanel-child').getStyleProperty('color')).eql('rgb(224, 224, 224)');

    openQuickAccessBar(t);
    writeQuickAccessBar(t, "Change Color Theme");
    writeQuickAccessBar(t, "Light");

    await t
        .expect(Selector('div.p-Widget.p-DockPanel.p-SplitPanel-child').getStyleProperty('color')).eql('rgb(97, 97, 97)');
});

test('Open graph-glsp.ecore', async t => {
    const wsSelect = Helper.wsSelect();
    const fileSelect = Selector('.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('glsp-graph.ecore');

    await t
        .click(wsSelect)
        .click(fileSelect)
        .expect(Selector('text.name.sprotty-label').withText('GSeverity').exists).ok('Class GSeverity exists')
        .expect(Selector('text.name.sprotty-label').count).eql(50);
});

test('Deletion/Renaming of enotation', async t => {
    const wsSelect = Helper.wsSelect();
    const fileSelect = Selector('.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('glsp-graph.enotation');
    const duplicateFile = Selector('.p-Menu-itemLabel').withText('Duplicate');
    const deleteFile = Selector('.p-Menu-itemLabel').withText('Delete');
    const renameFile = Selector('.p-Menu-itemLabel').withText('Rename');
    const okButton = Selector('.theia-button.main').withText('OK');
    const duplicateFileSelect = Selector('.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('glsp-graph_copy.enotation');
    const renameInput = Selector('div.dialogContent > input');

    await t
        .click(wsSelect)
        .rightClick(fileSelect)
        .click(duplicateFile)
        .wait(200)
        .rightClick(fileSelect)
        .click(deleteFile)
        .click(okButton)
        .expect(Selector('.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').count).eql(4)

        .rightClick(duplicateFileSelect)
        .click(renameFile)
        .pressKey('ctrl+a')
        .typeText(renameInput, "glsp-graph.enotation")
        .pressKey('Enter');

}).after(checkDefaultWorkbench);

test('Open Ecore without enotation', async t => {
    const wsSelect = Helper.wsSelect();
    const fileSelect = Selector('.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('UML.ecore');
    const classSelect = Selector("text.name.sprotty-label").withText("ConnectorKind").parent("g.node.ecore-node");

    await t
        .click(wsSelect)
        .click(fileSelect).wait(10000) // Necessary to wait as the layouting needs to be executed.
        .expect(classSelect.getAttribute('transform')).notEql('translate(0, 0)');
});

test('Create and Delete ecore file', async t => {
    const wsSelect = Helper.wsSelect();
    const deleteFile = Selector('.p-Menu-itemLabel').withText('Delete');
    const okButton = Selector('.theia-button.main').withText('OK');
    const newEcore = Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('Test.ecore');

    openQuickAccessBar(t);
    writeQuickAccessBar(t, "New Ecore-File");
    writeQuickAccessBar(t, "Test");
    writeQuickAccessBar(t, "testPrefix");
    writeQuickAccessBar(t, "testURI");

    await t
        .click(wsSelect)
        .wait(500)
        .expect(newEcore.exists).ok("File was not properply generated")
        .rightClick(newEcore)
        .click(deleteFile)
        .click(okButton)
        .expect(newEcore.exists).notOk("File was still found, even though it should be deleted");

}).after(checkDefaultWorkbench);

test('Create Nodes', async t => {
    const creator = new CreateHelper(t);
    await creator.createAllNodeTypes();
    await creator.layout();

    await t
        .expect(creator.nodesSelector.classNode.exists).ok("Class has been created")
        .expect(creator.nodesSelector.abstractNode.exists).ok("Abstract has been created")
        .expect(creator.nodesSelector.enumNode.exists).ok("Enum has been created")
        .expect(creator.nodesSelector.interfaceNode.exists).ok("Interface has been created")
        .pressKey('ctrl+s');

    // Check Serialization
    const fileSelect = Selector('.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('empty.enotation');
    const emptyFile = Selector('.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('empty.ecore');
    const openWith = Selector('.p-Menu-itemLabel').withText('Open With');
    const codeEditor = Selector('.p-Menu-itemLabel').withText('Code Editor');
    const line = Selector('.view-lines');

    // Enotation
    await t
        .click(fileSelect)
        .expect(line.child().child().withText('NewEClass0').exists).ok('Enotation serialization worked');

    // Ecore
    await t
        .rightClick(emptyFile)
        .hover(openWith)
        .click(codeEditor)
        .click(emptyFile)
        .expect(line.child().child().withText('NewEClass0').exists).ok('Ecore serialization worked');


    // Delete again
    const deleteFile = Selector('.p-Menu-itemLabel').withText('Delete');
    const okButton = Selector('.theia-button.main').withText('OK');
    creator.deleteAllNodes(t);
    await t
        .pressKey('ctrl+s')
        .rightClick(fileSelect)
        .click(deleteFile)
        .click(okButton);
}).after(checkDefaultWorkbench);

// Could check for Serialization
test('Move Class', async t => {
    const creator = new CreateHelper(t);
    await creator.createAllNodeTypes();
    await creator.layout();
    const drag_distance = 100;
    const original_pos = await creator.getNodePosition(creator.nodesSelector.classNode);
    await t
        .drag(creator.nodesSelector.classNode, drag_distance, drag_distance, { speed: 0.1 });
    const new_pos = await creator.getNodePosition(creator.nodesSelector.classNode);
    await t
        .expect(original_pos.x + drag_distance).eql(new_pos.x)
        .expect(original_pos.y + drag_distance).eql(new_pos.y);
});

test('Delete Nodes with Eraser', async t => {
    const creator = new CreateHelper(t);
    await creator.createAllNodeTypes();
    const eraser = Selector('.fas.fa-eraser.fa-xs');
    await creator.layout();
    await t
        .click(eraser)
        .click(creator.nodesSelector.classNode)
        .click(eraser)
        .click(creator.nodesSelector.abstractNode)
        .click(eraser)
        .click(creator.nodesSelector.enumNode)
        .click(eraser)
        .click(creator.nodesSelector.interfaceNode)
        .expect(creator.nodesSelector.classNode.exists).notOk("Class has been deleted")
        .expect(creator.nodesSelector.abstractNode.exists).notOk("Abstract has been deleted")
        .expect(creator.nodesSelector.enumNode.exists).notOk("Enum has been deleted")
        .expect(creator.nodesSelector.interfaceNode.exists).notOk("Interface has been deleted");
});

test('Delete Nodes with DEL', async t => {
    const creator = new CreateHelper(t);
    await creator.createAllNodeTypes();
    await creator.layout();
    await creator.deleteAllNodes(t);
    await t
        .expect(creator.nodesSelector.classNode.exists).notOk("Class has been deleted")
        .expect(creator.nodesSelector.abstractNode.exists).notOk("Abstract has been deleted")
        .expect(creator.nodesSelector.enumNode.exists).notOk("Enum has been deleted")
        .expect(creator.nodesSelector.interfaceNode.exists).notOk("Interface has been deleted");
});

test('Create Edges', async t => {
    const creator = new CreateHelper(t);
    await creator.createAllNodeTypes();

    await creator.createAllEdges();

    await t.expect(creator.edgeSelector.referenceEdge.exists).ok('Reference exists');
    await t.expect(creator.edgeSelector.containmentEdge.exists).ok('Containment exists');
    await t.expect(creator.edgeSelector.inheritanceEdge.exists).ok('Inheritance exists');
});

test('Move Edges', async t => {
    const creator = new CreateHelper(t);
    await creator.createAllNodeTypes();
    await creator.createAllEdges();
    await creator.layout();

    const points = Selector('.sprotty-edge.ecore-edge.selected').child().withAttribute('data-kind', 'manhattan-50%');
    const svgCanvas = Selector('svg.sprotty-graph');

    await t
        .click(creator.edgeSelector.referenceEdge);

    const original_pos1 = await creator.getEdgePosition(points.nth(0));
    const original_pos2 = await creator.getEdgePosition(points.nth(1));
    const original_pos3 = await creator.getEdgePosition(points.nth(2));

    await t
        .drag(points.nth(0), -10, 0, { speed: 0.1 })
        .click(svgCanvas)
        .click(creator.edgeSelector.referenceEdge)
        .drag(points.nth(1), 0, -10, { speed: 0.1 })
        .click(svgCanvas)
        .click(creator.edgeSelector.referenceEdge)
        .drag(points.nth(2), 10, 0, { speed: 0.1 })
        .click(svgCanvas)
        .click(creator.edgeSelector.referenceEdge);

    const new_pos1 = await creator.getEdgePosition(points.nth(0));
    const new_pos2 = await creator.getEdgePosition(points.nth(1));
    const new_pos3 = await creator.getEdgePosition(points.nth(2));

    await t
        .expect(original_pos1.x - 10).eql(new_pos1.x)
        .expect(original_pos1.y).eql(new_pos1.y)
        .expect(original_pos2.y - 10).eql(new_pos2.y)
        .expect(original_pos3.x + 10).eql(new_pos3.x);
});
test('Delete Edges', async t => {
    const creator = new CreateHelper(t);
    await creator.createAllNodeTypes();
    await creator.createAllEdges();
    const eraser = Selector('.fas.fa-eraser.fa-xs');
    await creator.layout();
    await t
        .click(eraser)
        .click(creator.edgeSelector.referenceEdge)
        .click(eraser)
        .click(creator.edgeSelector.containmentEdge)
        .click(eraser)
        .click(creator.edgeSelector.inheritanceEdge);
    await t.expect(creator.edgeSelector.referenceEdge.exists).notOk('Reference deleted');
    await t.expect(creator.edgeSelector.containmentEdge.exists).notOk('Containment deleted');
    await t.expect(creator.edgeSelector.inheritanceEdge.exists).notOk('Inheritance deleted');
});

test('Delete Edges with DEL', async t => {
    const creator = new CreateHelper(t);
    await creator.createAllNodeTypes();
    await creator.createAllEdges();
    await creator.layout();
    await t
        .click(creator.edgeSelector.referenceEdge)
        .pressKey('delete')
        .click(creator.edgeSelector.containmentEdge)
        .pressKey('delete')
        .click(creator.edgeSelector.inheritanceEdge)
        .pressKey('delete');
    await t.expect(creator.edgeSelector.referenceEdge.exists).notOk('Reference deleted');
    await t.expect(creator.edgeSelector.containmentEdge.exists).notOk('Containment deleted');
    await t.expect(creator.edgeSelector.inheritanceEdge.exists).notOk('Inheritance deleted');
});

test('Add Attributes/Literals', async t => {
    const creator = new CreateHelper(t);
    const attributeClass = Selector('g.node.ecore-node text.sprotty-label').withText('NewEAttribute4 : EString');
    const attributeAbstract = Selector('g.node.ecore-node text.sprotty-label').withText('NewEAttribute5 : EString');
    const attributeInterface = Selector('g.node.ecore-node text.sprotty-label').withText('NewEAttribute6 : EString');
    const attributeEnum = Selector('g.node.ecore-node text.sprotty-label').withText('NewEEnumLiteral7');

    await creator.createAllNodeTypes();
    await creator.addAttributes();

    await t
        .expect(attributeClass.exists).ok("Adding Attribute to Class")
        .expect(attributeAbstract.exists).ok("Adding Attribute to Abstract")
        .expect(attributeInterface.exists).ok("Adding Attribute to Interface")
        .expect(attributeEnum.exists).ok("Adding Literal to Enum");
});

// Could check for Serialization
test('Renaming Classes/Attributes', async t => {
    const creator = new CreateHelper(t);
    const nameClass = creator.nodesSelector.classNode;
    const nameAbstract = creator.nodesSelector.abstractNode;
    const nameInterface = creator.nodesSelector.interfaceNode;
    const nameEnum = creator.nodesSelector.enumNode;
    const attributeClass = Selector('g.node.ecore-node text.sprotty-label').withText('NewEAttribute4 : EString');
    const attributeAbstract = Selector('g.node.ecore-node text.sprotty-label').withText('NewEAttribute5 : EString');
    const attributeInterface = Selector('g.node.ecore-node text.sprotty-label').withText('NewEAttribute6 : EString');
    const attributeEnum = Selector('g.node.ecore-node text.sprotty-label').withText('NewEEnumLiteral7');
    const attributeClassRenamed = Selector('g.node.ecore-node text.sprotty-label').withText('TestAttributeClass : EString');
    const attributeAbstractRenamed = Selector('g.node.ecore-node text.sprotty-label').withText('TestAttributeAbstract : EString');
    const attributeInterfaceRenamed = Selector('g.node.ecore-node text.sprotty-label').withText('TestAttributeInterface : EString');
    const attributeEnumRenamed = Selector('g.node.ecore-node text.sprotty-label').withText('TestLiteralEnum');
    const nameClassRenamed = Selector('g.node.ecore-node text.name.sprotty-label').withText('TestClass');
    const nameAbstractRenamed = Selector('g.node.ecore-node text.name.sprotty-label').withText('TestAbstract');
    const nameInterfaceRenamed = Selector('g.node.ecore-node text.name.sprotty-label').withText('TestInterface');
    const nameEnumRenamed = Selector('g.node.ecore-node text.name.sprotty-label').withText('TestEnum');
    const input = Selector('div.label-edit input');

    await creator.createAllNodeTypes();
    await creator.addAttributes();

    await t
        .doubleClick(nameClass)
        .typeText(input, "TestClass")
        .click(nameAbstract)
        .doubleClick(nameAbstract)
        .typeText(input, "TestAbstract")
        .click(nameInterface)
        .doubleClick(nameInterface)
        .typeText(input, "TestInterface")
        .click(nameEnum)
        .doubleClick(nameEnum)
        .typeText(input, "TestEnum")
        .click(attributeClass)
        .doubleClick(attributeClass)
        .typeText(input, "TestAttributeClass")
        .click(attributeAbstract)
        .doubleClick(attributeAbstract)
        .typeText(input, "TestAttributeAbstract")
        .click(attributeInterface)
        .doubleClick(attributeInterface)
        .typeText(input, "TestAttributeInterface")
        .click(attributeEnum)
        .doubleClick(attributeEnum)
        .typeText(input, "TestLiteralEnum")
        .click(nameClassRenamed)
        .expect(nameClassRenamed.exists).ok("Renamed Class")
        .expect(nameAbstractRenamed.exists).ok("Renamed Abstract")
        .expect(nameInterfaceRenamed.exists).ok("Renamed Interface")
        .expect(nameEnumRenamed.exists).ok("Renamed Enum")
        .expect(attributeClassRenamed.exists).ok("Renamed Attribute in Class")
        .expect(attributeAbstractRenamed.exists).ok("Renamed Attribute in Abstract")
        .expect(attributeInterfaceRenamed.exists).ok("Renamed Attribute in Interface")
        .expect(attributeEnumRenamed.exists).ok("Renamed Literal in Enum");
});

// Could check for Serialization
test('Change Attributetype', async t => {
    const wsSelect = Helper.wsSelect();
    const glspEcore = Helper.glspGraphEcore();
    const attributeSelector = Selector('g.node.ecore-node text.sprotty-label').withText('layout : EString');
    const changedAttribute = Selector('g.node.ecore-node text.sprotty-label').withText('test : EDate');
    const changedAttributeWrite = Selector('g.node.ecore-node text.sprotty-label').withText('layout : EString');
    const input = Selector('div.label-edit input');

    await t
        .click(wsSelect)
        .click(glspEcore)
        .click(attributeSelector)
        .doubleClick(attributeSelector)
        .typeText(input, 'test : EDa')
        .pressKey('ctrl+space')
        .pressKey('down')
        .pressKey('Enter')
        .expect(changedAttribute.exists).ok("Changing the attributetype via Autocompletion")
        .doubleClick(changedAttribute)
        .typeText(input, 'layout : EString')
        .pressKey('Enter')
        .expect(changedAttributeWrite.exists).ok("Changing the attributetype via Typing");
});

test('Style new Diagram', async t => {
    const creator = new CreateHelper(t);
    await creator.createAllNodeTypes();
    await creator.createAllEdges();

    await t.pressKey('alt+l').wait(500)
        .expect(creator.nodesSelector.enumNode.parent("g.node.ecore-node").getAttribute('transform')).eql('translate(12, 12)')
        .expect(creator.nodesSelector.classNode.parent("g.node.ecore-node").getAttribute('transform')).notEql('translate(12, 12)')
        .expect(creator.nodesSelector.interfaceNode.parent("g.node.ecore-node").getAttribute('transform')).notEql('translate(12, 12)')
        .expect(creator.nodesSelector.abstractNode.parent("g.node.ecore-node").getAttribute('transform')).notEql('translate(12, 12)');
});
