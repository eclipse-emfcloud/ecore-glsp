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
const relPathToWorkspace = resolve(join(__dirname, 'workspace')).replace(/\\/g, "/");

const selectors = {
    emptyEcore: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('empty.ecore'),
    testEcore: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('test.ecore'),
    testEnotation: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('test.enotation'),
    testNodesOnlyEcore: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('testNodesOnly.ecore'),
    testNodesOnlyEnotation: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('testNodesOnly.enotation'),
    testNodesWithAttributesEcore: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('testNodesWithAttributes.ecore'),
    testNodesWithAttributesEnotation: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('testNodesWithAttributes.enotation'),
    umlEcore: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('UML.ecore'),
    wsSelect: Selector('#shell-tab-explorer-view-container'),
    svgCanvas: Selector('svg.sprotty-graph'),
    eraser: Selector('.fas.fa-eraser.fa-xs'),
    edgePoints: Selector('.sprotty-edge.ecore-edge.selected').child().withAttribute('data-kind', 'manhattan-50%'),
    duplicateFile: Selector('.p-Menu-itemLabel').withText('Duplicate'),
    deleteFile: Selector('.p-Menu-itemLabel').withText('Delete'),
    renameFile: Selector('.p-Menu-itemLabel').withText('Rename'),
    okButton: Selector('.theia-button.main').withText('OK'),
    renameInput: Selector('div.dialogContent > input'),
    openWith: Selector('.p-Menu-itemLabel').withText('Open With'),
    codeEditor: Selector('.p-Menu-itemLabel').withText('Code Editor'),
    line: Selector('.view-lines'),
    input: Selector('div.label-edit input'),
    attribute: Selector('div.tool-group > div.tool-button').withText("Attribute"),
    literal: Selector('div.tool-group > div.tool-button').withText("Literal"),
}

const nodesSelector = {
    classNode: Selector('g.node.ecore-node text.name.sprotty-label').withText('Class'),
    abstractNode: Selector('g.node.ecore-node.abstract text.name.sprotty-label').withText('Abstract'),
    enumNode: Selector('g.node.ecore-node text.name.sprotty-label').withText('Enum'),
    interfaceNode: Selector('g.node.ecore-node.interface text.name.sprotty-label').withText('Interface'),
    dataTypeNode: Selector('g.node.ecore-node text.name.sprotty-label').withText('DataType')
};

const edgeSelector = {
    inheritanceEdge: Selector('g.sprotty-edge.ecore-edge.inheritance'),
    containmentEdge: Selector('g.sprotty-edge.ecore-edge.composition text.edge-name.sprotty-label').withText('containment'),
    referenceEdge: Selector('g.sprotty-edge.ecore-edge text.edge-name.sprotty-label').withText('reference')
};

const attributeSelector = {
    attributeClass: Selector('g.node.ecore-node text.sprotty-label').withText('ClassAttribute : EString'),
    attributeAbstract: Selector('g.node.ecore-node text.sprotty-label').withText('AbstractAttribute : EString'),
    attributeInterface: Selector('g.node.ecore-node text.sprotty-label').withText('InterfaceAttribute : EString'),
    literalEnum: Selector('g.node.ecore-node text.sprotty-label').withText('EnumLiteral'),
}

const defaultNodesSelector = {
    classNode: Selector('g.node.ecore-node text.name.sprotty-label').withText('NewEClass0'),
    abstractNode: Selector('g.node.ecore-node.abstract text.name.sprotty-label').withText('NewEClass1'),
    interfaceNode: Selector('g.node.ecore-node.interface text.name.sprotty-label').withText('NewEClass2'),
    enumNode: Selector('g.node.ecore-node text.name.sprotty-label').withText('NewEEnum3'),
    dataTypeNode: Selector('g.node.ecore-node text.name.sprotty-label').withText('NewEDataType4')
};

const defaultEdgeSelector = {
    inheritanceEdge: Selector('g.sprotty-edge.ecore-edge.inheritance'),
    containmentEdge: Selector('g.sprotty-edge.ecore-edge.composition text.edge-name.sprotty-label').withText('classs'),
    referenceEdge: Selector('g.sprotty-edge.ecore-edge text.edge-name.sprotty-label').withText('abstracts')
};

const defaultAttributeSelector = {
    attributeClass: Selector('g.node.ecore-node text.sprotty-label').withText('NewEAttribute4 : EString'),
    attributeAbstract: Selector('g.node.ecore-node text.sprotty-label').withText('NewEAttribute5 : EString'),
    attributeInterface: Selector('g.node.ecore-node text.sprotty-label').withText('NewEAttribute6 : EString'),
    literalEnum: Selector('g.node.ecore-node text.sprotty-label').withText('NewEEnumLiteral7'),
}

const openFile = async (t, file) => {
    await t
        .click(selectors.wsSelect)
        .click(file);
}

const fileSelect = async (file) => {
    return Selector('.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText(file);
}

const checkDefaultWorkbench = async (t) => {
    await t
        .expect(selectors.emptyEcore.exists).ok('Check if empty.ecore exists')
        .expect(selectors.testEcore.exists).ok('Check if test.ecore exists')
        .expect(selectors.testEnotation.exists).ok('Check if test.enotation exists')
        .expect(selectors.testNodesOnlyEcore.exists).ok('Check if testNodesOnly.ecore exists')
        .expect(selectors.testNodesOnlyEnotation.exists).ok('Check if testNodesOnly.enotation exists')
        .expect(selectors.testNodesWithAttributesEcore.exists).ok('Check if testNodesWithAttributes.ecore exists')
        .expect(selectors.testNodesWithAttributesEnotation.exists).ok('Check if testNodesWith Attributes.enotation exists')
        .expect(selectors.umlEcore.exists).ok('Check if UML.ecore exists')
        .expect(Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').count).eql(8);
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
        .pressKey('Down')
        .pressKey('Enter');
};

const createNode = async (t, name, x, y) => {
    const selector = Selector('div.tool-group > div.tool-button').withText(name);
    const svgCanvas = Selector('svg.sprotty-graph');
    await t
        .click(selector)
        .click(svgCanvas, {offsetX : x, offsetY : y});
};

const deleteNode = async (t, selector) => {
    await t.click(selector).pressKey('delete');
};

const createEdge = async (t, name, a, b) => {
    const selector = Selector('div.tool-group > div.tool-button').withText(name);
    await t.click(selector).click(a).hover(b).click(b);
};

const getNodePosition = async (s: Selector) => {
    const transform = await (s.parent("g.node.ecore-node").getAttribute('transform'));
    const regex = new RegExp("\\s*\\w*\\s*\\(\\s*(\\d+(\\.\\d+)?)\\s*,\\s*(\\d+(\\.\\d+)?)\\s*\\)\\s*");
    const match = regex.exec(transform);
    if (match) {
        return { x: parseFloat(match[1]), y: parseFloat(match[3]) };
    }
    return {};
};

const getEdgePosition = async (s: Selector) => {
    const x = await (s.getAttribute('cx'));
    const y = await (s.getAttribute('cy'));
    return { x: parseFloat(x), y: parseFloat(y) };
};

const layout = async (t) => {
    await t
        .pressKey('alt+l')
        .wait(500);
};

const port = process.env.PORT || config.defaultPort;

fixture`Ecore-glsp E2E-Testing`// declare the fixture
    .page`http://localhost:${port}/#${relPathToWorkspace}`
    .after(async t => {
    });  // The start page loads the workbench at the above defined Path

test('Open Workbench', async t => {
    await t
        .wait(5000)
        .click(selectors.wsSelect);
    await checkDefaultWorkbench(t);
});

test('Switch Theme', async t => {

    openQuickAccessBar(t);
    writeQuickAccessBar(t, "Color Theme");
    writeQuickAccessBar(t, "Dark (Theia)");

    await t
        .expect(Selector('div.p-Widget.p-DockPanel.p-SplitPanel-child').getStyleProperty('color')).eql('rgb(204, 204, 204)');

    openQuickAccessBar(t);
    writeQuickAccessBar(t, "Color Theme");
    writeQuickAccessBar(t, "Light (Theia)");

    await t
        .expect(Selector('div.p-Widget.p-DockPanel.p-SplitPanel-child').getStyleProperty('color')).eql('rgb(97, 97, 97)');
});

test('Open UML.ecore (Autolayout/ Big Ecore)', async t => {
    openFile(t, selectors.umlEcore);

    await t
        .wait(10000) // Necessary to wait as the layouting needs to be executed.
        .expect(Selector('text.name.sprotty-label').withText('ConnectorKind').exists).ok('Class ConnectorKind exists')
        .expect(Selector('text.name.sprotty-label').count).eql(512)
        .expect(Selector('text.name.sprotty-label').withText('ConnectorKind').parent("g.node.ecore-node").getAttribute('transform')).eql('translate(12, 12)');
});

test('Deletion/Renaming of enotation', async t => {
    await t
        .click(selectors.wsSelect)
        .rightClick(selectors.testEnotation)
        .click(selectors.duplicateFile)
        .wait(200)
        .rightClick(selectors.testEnotation)
        .click(selectors.deleteFile)
        .click(selectors.okButton)
        .expect(Selector('.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').count).eql(8)
        .rightClick(await fileSelect('test_copy.enotation'))
        .click(selectors.renameFile)
        .pressKey('ctrl+a')
        .typeText(selectors.renameInput, "test.enotation", { replace: true })
        .pressKey('Enter');

}).after(checkDefaultWorkbench);

test('Create and Delete ecore file', async t => {
    const wsSelect = Selector('#shell-tab-explorer-view-container');
    const deleteFile = Selector('.p-Menu-itemLabel').withText('Delete');
    const okButton = Selector('.theia-button.main').withText('OK');
    const newEcore = Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('Creation.ecore');

    openQuickAccessBar(t);
    writeQuickAccessBar(t, "New Ecore-File");
    writeQuickAccessBar(t, "Creation");
    writeQuickAccessBar(t, "creationPrefix");
    writeQuickAccessBar(t, "creationURI");

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
    openFile(t, selectors.emptyEcore);

    createNode(t, "Class", 12, 12);
    createNode(t, "Abstract", 12, 62);
    createNode(t, "Interface", 12, 112);
    createNode(t, "Enum", 12, 162);
    createNode(t, "DataType", 12, 212);

    await t
        .expect(defaultNodesSelector.classNode.exists).ok("Class has been created")
        .expect(defaultNodesSelector.abstractNode.exists).ok("Abstract has been created")
        .expect(defaultNodesSelector.interfaceNode.exists).ok("Interface has been created")
        .expect(defaultNodesSelector.enumNode.exists).ok("Enum has been created")
        .expect(defaultNodesSelector.dataTypeNode.exists).ok("DataType has been created")
        .pressKey('ctrl+s');

    // Check Serialization

    // Enotation
    await t
        .click(await fileSelect('empty.enotation'))
        .expect(selectors.line.child().child().withText('NewEClass0').exists).ok('Enotation serialization worked')
        .expect(selectors.line.child().child().withText('NewEClass1').exists).ok('Enotation serialization worked')
        .expect(selectors.line.child().child().withText('NewEClass2').exists).ok('Enotation serialization worked')
        .expect(selectors.line.child().child().withText('NewEEnum3').exists).ok('Enotation serialization worked')
        .expect(selectors.line.child().child().withText('NewEDataType4').exists).ok('Enotation serialization worked');

    // Ecore
    await t
        .rightClick(selectors.emptyEcore)
        .hover(selectors.openWith)
        .click(selectors.codeEditor)
        .click(selectors.emptyEcore)
        .expect(selectors.line.child().child().withText('NewEClass0').exists).ok('Ecore serialization worked')
        .expect(selectors.line.child().child().withText('NewEClass1').exists).ok('Ecore serialization worked')
        .expect(selectors.line.child().child().withText('NewEClass2').exists).ok('Ecore serialization worked')
        .expect(selectors.line.child().child().withText('NewEEnum3').exists).ok('Ecore serialization worked')
        .expect(selectors.line.child().child().withText('NewEDataType4').exists).ok('Ecore serialization worked');


    // Delete again
    await t.wait(500);
    deleteNode(t, defaultNodesSelector.classNode);
    await t.wait(500);
    deleteNode(t, defaultNodesSelector.abstractNode);
    await t.wait(500);
    deleteNode(t, defaultNodesSelector.interfaceNode);
    await t.wait(500);
    deleteNode(t, defaultNodesSelector.enumNode);
    await t.wait(500);
    deleteNode(t, defaultNodesSelector.dataTypeNode);
    await t.wait(500);
    await t
        .pressKey('ctrl+s')
        .rightClick(await fileSelect('empty.enotation'))
        .click(selectors.deleteFile)
        .click(selectors.okButton);
}).after(checkDefaultWorkbench);

test('Create Edges', async t => {
    openFile(t, selectors.testNodesOnlyEcore);

    createEdge(t, 'Reference', nodesSelector.classNode, nodesSelector.abstractNode);
    createEdge(t, 'Inheritance', nodesSelector.interfaceNode, nodesSelector.abstractNode);
    createEdge(t, 'Containment', nodesSelector.interfaceNode, nodesSelector.classNode);

    await t
        .expect(defaultEdgeSelector.referenceEdge.exists).ok('Reference exists')
        .expect(defaultEdgeSelector.containmentEdge.exists).ok('Containment exists')
        .expect(defaultEdgeSelector.inheritanceEdge.exists).ok('Inheritance exists');
});

test('Add Attributes/Literals', async t => {
    openFile(t, selectors.testNodesOnlyEcore);

    await t
        .click(selectors.attribute)
        .click(nodesSelector.classNode)
        .click(selectors.attribute)
        .click(nodesSelector.abstractNode)
        .click(selectors.attribute)
        .click(nodesSelector.interfaceNode)
        .click(selectors.literal)
        .click(nodesSelector.enumNode);

    await t
        .expect(defaultAttributeSelector.attributeClass.exists).ok("Adding Attribute to Class")
        .expect(defaultAttributeSelector.attributeAbstract.exists).ok("Adding Attribute to Abstract")
        .expect(defaultAttributeSelector.attributeInterface.exists).ok("Adding Attribute to Interface")
        .expect(defaultAttributeSelector.literalEnum.exists).ok("Adding Literal to Enum");
});

test('Layout new Diagram', async t => {
    openFile(t, selectors.emptyEcore);
    createNode(t, "Class", 100, 100);
    createNode(t, "Abstract", 100, 100);
    createNode(t, "Interface", 100, 100);
    createNode(t, "Enum", 100, 100);

    layout(t);

    await t
        .expect(defaultNodesSelector.classNode.parent("g.node.ecore-node").getAttribute('transform')).eql('translate(12, 12)')
        .expect(defaultNodesSelector.enumNode.parent("g.node.ecore-node").getAttribute('transform')).notEql('translate(12, 12)')
        .expect(defaultNodesSelector.interfaceNode.parent("g.node.ecore-node").getAttribute('transform')).notEql('translate(12, 12)')
        .expect(defaultNodesSelector.abstractNode.parent("g.node.ecore-node").getAttribute('transform')).notEql('translate(12, 12)');
});

test('Move Class', async t => {
    openFile(t, selectors.testEcore);
    await t.wait(500);
    const drag_distance = 100;
    const original_pos = await getNodePosition(nodesSelector.classNode);
    await t
        .drag(nodesSelector.classNode, drag_distance, drag_distance, { speed: 0.1 });
    const new_pos = await getNodePosition(nodesSelector.classNode);
    await t
        .expect(original_pos.x + drag_distance).eql(new_pos.x)
        .expect(original_pos.y + drag_distance).eql(new_pos.y);
});

test('Move Edges', async t => {
    openFile(t, selectors.testEcore);

    await t
        .click(edgeSelector.referenceEdge);

    const original_pos1 = await getEdgePosition(selectors.edgePoints.nth(0));
    const original_pos2 = await getEdgePosition(selectors.edgePoints.nth(1));
    const original_pos3 = await getEdgePosition(selectors.edgePoints.nth(2));

    await t
        .drag(selectors.edgePoints.nth(0), 0, -5, { speed: 0.1 })
        .click(selectors.svgCanvas)
        .click(edgeSelector.referenceEdge)
        .drag(selectors.edgePoints.nth(1), -10, 0, { speed: 0.1 })
        .click(selectors.svgCanvas)
        .click(edgeSelector.referenceEdge)
        .drag(selectors.edgePoints.nth(2), 0, 5, { speed: 0.1 })
        .click(selectors.svgCanvas)
        .click(edgeSelector.referenceEdge);

    const new_pos1 = await getEdgePosition(selectors.edgePoints.nth(0));
    const new_pos2 = await getEdgePosition(selectors.edgePoints.nth(1));
    const new_pos3 = await getEdgePosition(selectors.edgePoints.nth(2));

    await t
        .expect(original_pos1.y).notEql(new_pos1.y)
        .expect(original_pos2.x).notEql(new_pos2.x)
        .expect(original_pos3.y).notEql(new_pos3.y);
});

test('Renaming Classes/Attributes', async t => {

    const attributeClassRenamed = Selector('g.node.ecore-node text.sprotty-label').withText('TestAttributeClass : EString');
    const attributeAbstractRenamed = Selector('g.node.ecore-node text.sprotty-label').withText('TestAttributeAbstract : EString');
    const attributeInterfaceRenamed = Selector('g.node.ecore-node text.sprotty-label').withText('TestAttributeInterface : EString');
    const attributeEnumRenamed = Selector('g.node.ecore-node text.sprotty-label').withText('TestLiteralEnum');

    const nameClassRenamed = Selector('g.node.ecore-node text.name.sprotty-label').withText('TestClass');
    const nameAbstractRenamed = Selector('g.node.ecore-node text.name.sprotty-label').withText('TestAbstract');
    const nameInterfaceRenamed = Selector('g.node.ecore-node text.name.sprotty-label').withText('TestInterface');
    const nameEnumRenamed = Selector('g.node.ecore-node text.name.sprotty-label').withText('TestEnum');

    openFile(t, selectors.testNodesWithAttributesEcore);

    await t
        .click(nodesSelector.classNode)
        .doubleClick(nodesSelector.classNode)
        .typeText(selectors.input, "TestClass", { replace: true })
        .click(nodesSelector.abstractNode)
        .doubleClick(nodesSelector.abstractNode)
        .typeText(selectors.input, "TestAbstract", { replace: true })
        .click(nodesSelector.interfaceNode)
        .doubleClick(nodesSelector.interfaceNode)
        .typeText(selectors.input, "TestInterface", { replace: true })
        .click(nodesSelector.enumNode)
        .doubleClick(nodesSelector.enumNode)
        .typeText(selectors.input, "TestEnum", { replace: true })
        .click(attributeSelector.attributeClass)
        .doubleClick(attributeSelector.attributeClass)
        .typeText(selectors.input, "TestAttributeClass", { replace: true })
        .click(attributeSelector.attributeAbstract)
        .doubleClick(attributeSelector.attributeAbstract)
        .typeText(selectors.input, "TestAttributeAbstract", { replace: true })
        .click(attributeSelector.attributeInterface)
        .doubleClick(attributeSelector.attributeInterface)
        .typeText(selectors.input, "TestAttributeInterface", { replace: true })
        .click(attributeSelector.literalEnum)
        .doubleClick(attributeSelector.literalEnum)
        .typeText(selectors.input, "TestLiteralEnum", { replace: true })
        .click(selectors.svgCanvas)
        .expect(nameClassRenamed.exists).ok("Renamed Class")
        .expect(nameAbstractRenamed.exists).ok("Renamed Abstract")
        .expect(nameInterfaceRenamed.exists).ok("Renamed Interface")
        .expect(nameEnumRenamed.exists).ok("Renamed Enum")
        .expect(attributeClassRenamed.exists).ok("Renamed Attribute in Class")
        .expect(attributeAbstractRenamed.exists).ok("Renamed Attribute in Abstract")
        .expect(attributeInterfaceRenamed.exists).ok("Renamed Attribute in Interface")
        .expect(attributeEnumRenamed.exists).ok("Renamed Literal in Enum");
});

test('Change Attributetype', async t => {
    openFile(t, selectors.testNodesWithAttributesEcore)
    const changedAttribute = Selector('g.node.ecore-node text.sprotty-label').withText('test : EDate');
    const changedAttributeWrite = Selector('g.node.ecore-node text.sprotty-label').withText('layout : EString');

    await t
        .click(attributeSelector.attributeClass)
        .doubleClick(attributeSelector.attributeClass)
        .typeText(selectors.input, 'test : EDa', { replace: true })
        .pressKey('ctrl+space')
        .pressKey('down')
        .pressKey('Enter')
        .expect(changedAttribute.exists).ok("Changing the attributetype via Autocompletion")
        .doubleClick(changedAttribute)
        .typeText(selectors.input, 'layout : EString', { replace: true })
        .pressKey('Enter')
        .expect(changedAttributeWrite.exists).ok("Changing the attributetype via Typing");
});

test('Delete Nodes with Eraser', async t => {
    openFile(t, selectors.testEcore);
    await t
        .click(selectors.eraser)
        .click(nodesSelector.classNode, { offsetY: -40 })
        .click(selectors.eraser)
        .click(nodesSelector.abstractNode, { offsetY: -40 })
        .click(selectors.eraser)
        .click(nodesSelector.enumNode, { offsetY: -40 })
        .click(selectors.eraser)
        .click(nodesSelector.interfaceNode, { offsetY: -40 })
        .expect(nodesSelector.classNode.exists).notOk("Class has been deleted")
        .expect(nodesSelector.abstractNode.exists).notOk("Abstract has been deleted")
        .expect(nodesSelector.enumNode.exists).notOk("Enum has been deleted")
        .expect(nodesSelector.interfaceNode.exists).notOk("Interface has been deleted");
});

test('Delete Nodes with DEL', async t => {
    openFile(t, selectors.testEcore);
    await t
        .wait(200)
        .click(nodesSelector.classNode, { offsetY: -40 })
        .pressKey('delete')
        .wait(200)
        .click(nodesSelector.abstractNode, { offsetY: -40 })
        .pressKey('delete')
        .wait(200)
        .click(nodesSelector.interfaceNode, { offsetY: -40 })
        .pressKey('delete')
        .wait(200)
        .click(nodesSelector.enumNode, { offsetY: -40 })
        .pressKey('delete')
        .wait(200)
        .expect(nodesSelector.classNode.exists).notOk("Class has been deleted")
        .expect(nodesSelector.abstractNode.exists).notOk("Abstract has been deleted")
        .expect(nodesSelector.interfaceNode.exists).notOk("Interface has been deleted")
        .expect(nodesSelector.enumNode.exists).notOk("Enum has been deleted");
});

test('Delete Edges', async t => {
    openFile(t, selectors.testEcore);
    await t
        .click(selectors.eraser)
        .click(nodesSelector.classNode, { offsetX: 90 })
        .click(selectors.eraser)
        .click(nodesSelector.classNode, { offsetY: 60, offsetX: 15 })
        .click(selectors.eraser)
        .click(nodesSelector.interfaceNode, { offsetY: -60, offsetX: 28 })
        .expect(edgeSelector.referenceEdge.exists).notOk('Reference deleted')
        .expect(edgeSelector.containmentEdge.exists).notOk('Containment deleted')
        .expect(edgeSelector.inheritanceEdge.exists).notOk('Inheritance deleted');
});

test('Delete Edges with DEL', async t => {
    openFile(t, selectors.testEcore);
    await t
        .click(edgeSelector.referenceEdge)
        .pressKey('delete')
        .click(edgeSelector.containmentEdge)
        .pressKey('delete')
        .click(edgeSelector.inheritanceEdge)
        .pressKey('delete')
        .expect(edgeSelector.containmentEdge.exists).notOk('Containment deleted')
        .expect(edgeSelector.inheritanceEdge.exists).notOk('Inheritance deleted')
        .expect(edgeSelector.referenceEdge.exists).notOk('Reference deleted');
});