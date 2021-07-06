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
import { ClientFunction, Selector } from "testcafe";

import * as config from "./config.json";

const selectors = {
    treeNode: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow'),
    emptyEcore: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('empty.ecore'),
    emptyEnotation: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('empty.enotation'),
    testEcore: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('test.ecore'),
    testEnotation: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('test.enotation'),
    testNodesOnlyEcore: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('testNodesOnly.ecore'),
    testNodesOnlyEnotation: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('testNodesOnly.enotation'),
    testNodesWithAttributesEcore: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('testNodesWithAttributes.ecore'),
    testNodesWithAttributesEnotation: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('testNodesWithAttributes.enotation'),
    umlEcore: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('UML.ecore'),
    umlEnotation: Selector('div.theia-TreeNodeSegment.theia-TreeNodeSegmentGrow').withText('UML.enotation'),
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
    attribute: Selector('div.tool-group > div.tool-button').withText('Attribute'),
    literal: Selector('div.tool-group > div.tool-button').withText('Literal'),
    firstEditorTab: Selector('div.p-Widget.p-TabBar.theia-app-centers.theia-app-main > div.p-TabBar-content-container.ps').child('ul').child(':first-child'),
    close: Selector('.p-Menu-itemLabel').withText('Close'),
    saveButton: Selector('.theia-button.main').withText('Save'),
    dontSaveButton: Selector('.theia-button.secondary').withText('Don\'t save'),
    menuBarItem: Selector('.p-Menu.p-MenuBar-menu').child('p-MenuBar-itemLabel'),
    undo: Selector('.p-Menu-itemLabel').withText('Undo')
}

const nodesSelector = {
    classNode: Selector('g.node.ecore-node.class text.name.sprotty-label').withText('Class'),
    abstractNode: Selector('g.node.ecore-node.abstract.class text.name.sprotty-label').withText('Abstract'),
    enumNode: Selector('g.node.ecore-node.enum text.name.sprotty-label').withText('Enum'),
    interfaceNode: Selector('g.node.ecore-node.interface.class text.name.sprotty-label').withText('Interface'),
    dataTypeNode: Selector('g.node.ecore-node.datatype text.name.sprotty-label').withText('DataType')
};

const edgeSelector = {
    inheritanceEdge: Selector('g.sprotty-edge.ecore-edge.inheritance'),
    containmentEdge: Selector('g.sprotty-edge.ecore-edge.composition text.edge-name.sprotty-label').withText('containment'),
    referenceEdge: Selector('g.sprotty-edge.ecore-edge text.edge-name.sprotty-label').withText('reference')
};

const attributeSelector = {
    attributeClass: Selector('g.node.ecore-node text.sprotty-label').withText('ClassAttribute'),
    attributeAbstract: Selector('g.node.ecore-node text.sprotty-label').withText('AbstractAttribute'),
    attributeInterface: Selector('g.node.ecore-node text.sprotty-label').withText('InterfaceAttribute'),
    literalEnum: Selector('g.node.ecore-node text.sprotty-label').withText('EnumLiteral'),
}

const defaultNodesSelector = {
    classNode: Selector('g.node.ecore-node.class text.name.sprotty-label').withText('NewEClass0'),
    abstractNode: Selector('g.node.ecore-node.abstract.class text.name.sprotty-label').withText('NewEClass1'),
    interfaceNode: Selector('g.node.ecore-node.interface.class text.name.sprotty-label').withText('NewEClass2'),
    enumNode: Selector('g.node.ecore-node.enum text.name.sprotty-label').withText('NewEEnum3'),
    dataTypeNode: Selector('g.node.ecore-node.datatype text.name.sprotty-label').withText('NewEDataType4')
};

const defaultEdgeSelector = {
    inheritanceEdge: Selector('g.sprotty-edge.ecore-edge.inheritance'),
    containmentEdge: Selector('g.sprotty-edge.ecore-edge.composition text.edge-name.sprotty-label').withText('classs'),
    referenceEdge: Selector('g.sprotty-edge.ecore-edge text.edge-name.sprotty-label').withText('abstracts')
};

const defaultAttributeSelector = {
    attributeClass: Selector('g.node.ecore-node text.sprotty-label').withText('newEAttribute4'),
    attributeAbstract: Selector('g.node.ecore-node text.sprotty-label').withText('newEAttribute5'),
    attributeInterface: Selector('g.node.ecore-node text.sprotty-label').withText('newEAttribute6'),
    literalEnum: Selector('g.node.ecore-node text.sprotty-label').withText('newEEnumLiteral7'),
}

const defaultActionSpeed = 0.75;
const defaultMouseDragSpeed = 0.1;
const defaultTypingOptions: TypeActionOptions = { replace: true, speed: defaultActionSpeed };

const openWorkbench = async (t: TestController) => {
    let navigatorButton = await selectors.wsSelect.with({ visibilityCheck: true, timeout: 30000 })();
    if (!navigatorButton || (navigatorButton && !navigatorButton.visible)) {
        await t.wait(30000);
        navigatorButton = await selectors.wsSelect.with({ visibilityCheck: true, timeout: 30000 })();
        if (!navigatorButton || (navigatorButton && !navigatorButton.visible)) {
            throw new Error('Theia application workspace could not be loaded!');
        }
    }
    await t.click(navigatorButton);
    await checkDefaultWorkbench(t);
};

const openFile = async (t: TestController, file: Selector, shouldOpenWorkbench = false) => {
    if (shouldOpenWorkbench) {
        await openWorkbench(t);
    }
    await t.click(file);
}

const closeEditor = async (t: TestController, isDirty = false, saveOnClose = false) => {
    const editorTab = await selectors.firstEditorTab();
    await t.rightClick(editorTab)
        .hover(selectors.close)
        .click(selectors.close);

    if (isDirty) {
        await t.click(saveOnClose ? selectors.saveButton : selectors.dontSaveButton);
    }
}

const closeEditorWithoutSave = async (t: TestController) => {
    await closeEditor(t, true /*isDirty*/, false /*don't save*/);
};

const undo = async (t: TestController, times = 1) => {
    await focus();
    for (let i = 0; i < times; i++) {
        await t
            .pressKey('ctrl+z')
            .wait(200);
    }
}

const deleteFile = async (t: TestController, fileSelector: Selector) => {
    await t
        .rightClick(fileSelector)
        .click(selectors.deleteFile)
        .click(selectors.okButton);
}

const duplicateFile = async (t: TestController, fileSelector: Selector) => {
    await t
        .rightClick(fileSelector)
        .click(selectors.duplicateFile)
        .wait(200);
}

const renameFile = async (t: TestController, fileSelector: Selector, newFileName: string) => {
    await t
        .expect(selectors.treeNode.count).eql(10)
        .rightClick(fileSelector)
        .click(selectors.renameFile)
        .pressKey('ctrl+a')
        .typeText(selectors.renameInput, newFileName, defaultTypingOptions)
        .pressKey('Enter');
}

const checkDefaultWorkbench = async (t: TestController) => {
    await t
        .expect(selectors.emptyEcore.exists).ok('Check if empty.ecore exists')
        .expect(selectors.testEcore.exists).ok('Check if test.ecore exists')
        .expect(selectors.testEnotation.exists).ok('Check if test.enotation exists')
        .expect(selectors.testNodesOnlyEcore.exists).ok('Check if testNodesOnly.ecore exists')
        .expect(selectors.testNodesOnlyEnotation.exists).ok('Check if testNodesOnly.enotation exists')
        .expect(selectors.testNodesWithAttributesEcore.exists).ok('Check if testNodesWithAttributes.ecore exists')
        .expect(selectors.testNodesWithAttributesEnotation.exists).ok('Check if testNodesWith Attributes.enotation exists')
        .expect(selectors.umlEcore.exists).ok('Check if UML.ecore exists')
        .expect(selectors.treeNode.count).eql(10)
        .expect(selectors.firstEditorTab.exists).notOk('Check all open editors are closed');
};

const openQuickAccessBar = async (t: TestController) => {
    const viewMenu = Selector('.p-MenuBar-item').withText('View');
    const findCommand = Selector('.p-Menu-itemLabel').withText('Find Command...');

    await t
        .click(viewMenu)
        .click(findCommand);
};

const writeQuickAccessBar = async (t: TestController, text: string) => {
    const quickAccess = Selector('div.monaco-inputbox.idle input.input');

    await t
        .typeText(quickAccess, text, { speed: defaultActionSpeed })
        .pressKey('Down')
        .pressKey('Enter');
};

const createNode = async (t: TestController, name: string, x: number, y: number) => {
    const selector = Selector('div.tool-group > div.tool-button').withText(name);
    const svgCanvas = Selector('svg.sprotty-graph');
    await t
        .click(selector)
        .click(svgCanvas, { offsetX: x, offsetY: y })
        .wait(200);
};

const deleteNode = async (t: TestController, selector: Selector) => {
    await t.click(selector).pressKey('delete');
};

const createEdge = async (t: TestController, name: string, source: Selector, target: Selector) => {
    const selector = Selector('div.tool-group > div.tool-button').withText(name);
    await t.click(selector).click(source).hover(target).click(target);
};

const getNodePosition = async (s: Selector) => {
    const transform = await (s.parent('g.node.ecore-node').getAttribute('transform'));
    const regex = new RegExp('\\s*\\w*\\s*\\(\\s*(\\d+(\\.\\d+)?)\\s*,\\s*(\\d+(\\.\\d+)?)\\s*\\)\\s*');
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

const layout = async (t: TestController) => {
    await t
        .pressKey('alt+l')
        .wait(500);
};

var focus = ClientFunction(() => {
    (document.querySelector('div.sprotty[id^="ecorediagram_"]') as HTMLElement).focus();
});

const port = process.env.PORT || config.defaultPort;

// declare the fixture
fixture.disablePageReloads`Ecore-glsp E2E-Testing`
    .page`http://localhost:${port}`
    .beforeEach(async t => {
        await t.resizeWindow(1920, 1080);
    });

test('Open Workbench', async t => {
    await openWorkbench(t);
});

test.skip('Switch Theme', async t => {
    await openQuickAccessBar(t);
    await writeQuickAccessBar(t, 'Color Theme');
    await writeQuickAccessBar(t, 'Dark (Theia)');

    await t
        .expect(Selector('div.p-Widget.p-DockPanel.p-SplitPanel-child').getStyleProperty('color')).eql('rgb(204, 204, 204)');

    await openQuickAccessBar(t);
    await writeQuickAccessBar(t, 'Color Theme');
    await writeQuickAccessBar(t, 'Light (Theia)');

    await t
        .expect(Selector('div.p-Widget.p-DockPanel.p-SplitPanel-child').getStyleProperty('color')).eql('rgb(97, 97, 97)');
});

test('Open UML.ecore (Autolayout/ Big Ecore)', async t => {
    await openFile(t, selectors.umlEcore);

    await t
        .wait(10000) // Necessary to wait as the layouting needs to be executed.
        .expect(Selector('text.name.sprotty-label').withText('ConnectorKind').exists).ok('Class ConnectorKind exists')
        .expect(Selector('g.node.ecore-node.enum').count).eql(26)
        .expect(Selector('g.node.ecore-node.class').count).eql(486)
        .expect(Selector('text.name.sprotty-label').withText('ConnectorKind').parent('g.node.ecore-node').getAttribute('transform')).eql('translate(12, 12)')
        .wait(500);

    await closeEditorWithoutSave(t);
    await deleteFile(t, selectors.umlEnotation);

}).after(checkDefaultWorkbench);

test('Deletion/Renaming of enotation', async t => {
    await checkDefaultWorkbench(t);
    await duplicateFile(t, selectors.testEnotation);
    await deleteFile(t, selectors.testEnotation);
    await renameFile(t, selectors.treeNode.withText('test_copy.enotation'), 'test.enotation')

}).after(checkDefaultWorkbench);

test('Create and Delete ecore diagram files', async t => {
    const newEcoreFolder = selectors.treeNode.withText('Creation');
    const modelFolder = selectors.treeNode.withText('model');
    const newEcore = selectors.treeNode.withText('Creation.ecore');
    const newEnotation = selectors.treeNode.withText('Creation.enotation');

    await checkDefaultWorkbench(t);
    await openQuickAccessBar(t);
    await writeQuickAccessBar(t, 'New Ecore Model Diagram');
    await writeQuickAccessBar(t, 'Creation');
    await writeQuickAccessBar(t, 'creationPrefix');
    try {
        await writeQuickAccessBar(t, 'creationURI');
    } catch (e) {
        // Theia error occurs during testcafe execution: Error: The command 'navigator.reveal' cannot be executed.
        console.log('Known Theia error during testcafe execution: ' + e);
    }

    await t
        .expect(newEcoreFolder.exists).ok('Diagram files were not properply generated')
        .click(newEcoreFolder)
        .wait(200)
        .expect(modelFolder.exists).ok('Diagram files were not properply generated')
        .expect(newEcore.exists).ok('Ecore file was not properply generated')
        .expect(newEnotation.exists).ok('Enotation file was not properply generated')
        .wait(200);

    await deleteFile(t, newEcoreFolder);

    await t.expect(newEcore.exists).notOk('Files were still found, even though they should be deleted');

}).after(checkDefaultWorkbench);

test('Create Nodes', async t => {
    await openFile(t, selectors.emptyEcore);

    await createNode(t, 'Class', 12, 12);
    await createNode(t, 'Abstract', 12, 62);
    await createNode(t, 'Interface', 12, 112);
    await createNode(t, 'Enum', 12, 162);
    await createNode(t, 'DataType', 12, 212);

    await t
        .expect(defaultNodesSelector.classNode.exists).ok('Class has been created')
        .expect(defaultNodesSelector.abstractNode.exists).ok('Abstract has been created')
        .expect(defaultNodesSelector.interfaceNode.exists).ok('Interface has been created')
        .expect(defaultNodesSelector.enumNode.exists).ok('Enum has been created')
        .expect(defaultNodesSelector.dataTypeNode.exists).ok('DataType has been created')
        .pressKey('ctrl+s');

    // Check Serialization

    // Enotation
    await openFile(t, selectors.emptyEnotation);
    await t
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

    // Open diagram again
    await openFile(t, selectors.emptyEcore);
    await focus();

    // Delete again
    await t.wait(500);
    await deleteNode(t, defaultNodesSelector.classNode);
    await t.wait(500);
    await deleteNode(t, defaultNodesSelector.abstractNode);
    await t.wait(500);
    await deleteNode(t, defaultNodesSelector.interfaceNode);
    await t.wait(500);
    await deleteNode(t, defaultNodesSelector.enumNode);
    await t.wait(500);
    await deleteNode(t, defaultNodesSelector.dataTypeNode);
    await t.wait(500);
    await t.pressKey('ctrl+s');

    await closeEditor(t);
    await closeEditor(t);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Create Reference', async t => {
    await openFile(t, selectors.testNodesOnlyEcore);
    await createEdge(t, 'Reference', nodesSelector.classNode, nodesSelector.abstractNode);
    await t.expect(defaultEdgeSelector.referenceEdge.exists).ok('Reference exists');

    await undo(t);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Create Inheritance', async t => {
    await openFile(t, selectors.testNodesOnlyEcore);
    await createEdge(t, 'Inheritance', nodesSelector.classNode, nodesSelector.abstractNode);
    await t.expect(defaultEdgeSelector.inheritanceEdge.exists).ok('Inheritance exists');

    await undo(t);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Create Containment', async t => {
    await openFile(t, selectors.testNodesOnlyEcore);
    await createEdge(t, 'Containment', nodesSelector.interfaceNode, nodesSelector.classNode);
    await t.expect(defaultEdgeSelector.containmentEdge.exists).ok('Containment exists');

    await undo(t);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Add Attributes/Literals', async t => {
    await openFile(t, selectors.testNodesOnlyEcore);

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
        .expect(defaultAttributeSelector.attributeClass.exists).ok('Adding Attribute to Class')
        .expect(defaultAttributeSelector.attributeAbstract.exists).ok('Adding Attribute to Abstract')
        .expect(defaultAttributeSelector.attributeInterface.exists).ok('Adding Attribute to Interface')
        .expect(defaultAttributeSelector.literalEnum.exists).ok('Adding Literal to Enum');

    await undo(t, 4);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Layout new Diagram', async t => {
    await openFile(t, selectors.emptyEcore);
    await createNode(t, 'Class', 100, 100);
    await createNode(t, 'Abstract', 100, 100);
    await createNode(t, 'Interface', 100, 100);
    await createNode(t, 'Enum', 100, 100);

    await layout(t);

    await t
        .expect(defaultNodesSelector.classNode.parent('g.node.ecore-node').getAttribute('transform')).eql('translate(12, 12)')
        .expect(defaultNodesSelector.enumNode.parent('g.node.ecore-node').getAttribute('transform')).notEql('translate(12, 12)')
        .expect(defaultNodesSelector.interfaceNode.parent('g.node.ecore-node').getAttribute('transform')).notEql('translate(12, 12)')
        .expect(defaultNodesSelector.abstractNode.parent('g.node.ecore-node').getAttribute('transform')).notEql('translate(12, 12)');

    await undo(t, 5);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Move Class', async t => {
    await openFile(t, selectors.testEcore);
    await t.wait(500);
    const drag_distance = 100;
    const original_pos = await getNodePosition(nodesSelector.classNode);
    await t
        .drag(nodesSelector.classNode, drag_distance, drag_distance, { speed: defaultMouseDragSpeed });
    const new_pos = await getNodePosition(nodesSelector.classNode);
    await t
        .expect(original_pos.x + drag_distance).eql(new_pos.x)
        .expect(original_pos.y + drag_distance).eql(new_pos.y);

    await undo(t, 2);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Move Edges', async t => {
    await openFile(t, selectors.testEcore);
    await t.wait(500);
    await t.click(edgeSelector.referenceEdge);

    const original_pos1 = await getEdgePosition(selectors.edgePoints.nth(0));
    const original_pos2 = await getEdgePosition(selectors.edgePoints.nth(1));

    await t
        .wait(500)
        .drag(selectors.edgePoints.nth(0), 15, 0, { speed: defaultMouseDragSpeed })
        .wait(500)
        .click(selectors.svgCanvas)
        .click(edgeSelector.referenceEdge)
        .wait(500)
        .drag(selectors.edgePoints.nth(1), 0, -10, { speed: defaultMouseDragSpeed })
        .wait(500)
        .click(selectors.svgCanvas)
        .click(edgeSelector.referenceEdge);

    const new_pos1 = await getEdgePosition(selectors.edgePoints.nth(0));
    const new_pos2 = await getEdgePosition(selectors.edgePoints.nth(1));

    await t
        .expect(original_pos1).notEql(new_pos1)
        .expect(original_pos2).notEql(new_pos2);

    await undo(t, 4);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Renaming Classes/Attributes', async t => {

    const attributeClassRenamed = Selector('g.node.ecore-node text.sprotty-label').withText('TestAttributeClass : EString');
    const attributeAbstractRenamed = Selector('g.node.ecore-node text.sprotty-label').withText('TestAttributeAbstract : EString');
    const attributeInterfaceRenamed = Selector('g.node.ecore-node text.sprotty-label').withText('TestAttributeInterface');
    const attributeEnumRenamed = Selector('g.node.ecore-node text.sprotty-label').withText('TestLiteralEnum');

    const nameClassRenamed = Selector('g.node.ecore-node text.name.sprotty-label').withText('TestClass');
    const nameAbstractRenamed = Selector('g.node.ecore-node text.name.sprotty-label').withText('TestAbstract');
    const nameInterfaceRenamed = Selector('g.node.ecore-node text.name.sprotty-label').withText('TestInterface');
    const nameEnumRenamed = Selector('g.node.ecore-node text.name.sprotty-label').withText('TestEnum');

    await openFile(t, selectors.testNodesWithAttributesEcore);

    await t
        .click(nodesSelector.classNode)
        .doubleClick(nodesSelector.classNode)
        .typeText(selectors.input, 'TestClass',)
        .pressKey('Enter')
        .click(nodesSelector.abstractNode)
        .doubleClick(nodesSelector.abstractNode)
        .typeText(selectors.input, 'TestAbstract', defaultTypingOptions)
        .pressKey('Enter')
        .click(nodesSelector.interfaceNode)
        .doubleClick(nodesSelector.interfaceNode)
        .typeText(selectors.input, 'TestInterface', defaultTypingOptions)
        .pressKey('Enter')
        .click(nodesSelector.enumNode)
        .doubleClick(nodesSelector.enumNode)
        .typeText(selectors.input, 'TestEnum', defaultTypingOptions)
        .pressKey('Enter')
        .click(attributeSelector.attributeClass)
        .doubleClick(attributeSelector.attributeClass)
        .typeText(selectors.input, 'TestAttributeClass : EString', defaultTypingOptions)
        .pressKey('Enter')
        .click(attributeSelector.attributeAbstract)
        .doubleClick(attributeSelector.attributeAbstract)
        .typeText(selectors.input, 'TestAttributeAbstract : EString', defaultTypingOptions)
        .pressKey('Enter')
        .click(attributeSelector.attributeInterface)
        .doubleClick(attributeSelector.attributeInterface)
        .typeText(selectors.input, 'TestAttributeInterface', defaultTypingOptions)
        .pressKey('Enter')
        .click(attributeSelector.literalEnum)
        .doubleClick(attributeSelector.literalEnum)
        .typeText(selectors.input, 'TestLiteralEnum', defaultTypingOptions)
        .pressKey('Enter')
        .click(selectors.svgCanvas)
        .expect(nameClassRenamed.exists).ok('Renamed Class')
        .expect(nameAbstractRenamed.exists).ok('Renamed Abstract')
        .expect(nameInterfaceRenamed.exists).ok('Renamed Interface')
        .expect(nameEnumRenamed.exists).ok('Renamed Enum')
        .expect(attributeClassRenamed.exists).ok('Renamed Attribute in Class')
        .expect(attributeAbstractRenamed.exists).ok('Renamed Attribute in Abstract')
        .expect(attributeInterfaceRenamed.exists).ok('Renamed Attribute in Interface')
        .expect(attributeEnumRenamed.exists).ok('Renamed Literal in Enum');

    await undo(t, 8);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Change Attributetype', async t => {
    await openFile(t, selectors.testNodesWithAttributesEcore)
    const changedAttribute = Selector('g.node.ecore-node text.sprotty-label').withText('test : EDate');
    const changedAttributeWrite = Selector('g.node.ecore-node text.sprotty-label').withText('layout : EString');

    await t
        .click(attributeSelector.attributeClass)
        .doubleClick(attributeSelector.attributeClass)
        .typeText(selectors.input, 'test : EDa', defaultTypingOptions)
        .pressKey('ctrl+space')
        .pressKey('down')
        .pressKey('Enter')
        .expect(changedAttribute.exists).ok('Changing the attributetype via Autocompletion')
        .doubleClick(changedAttribute)
        .typeText(selectors.input, 'layout : EString', defaultTypingOptions)
        .pressKey('Enter')
        .expect(changedAttributeWrite.exists).ok('Changing the attributetype via Typing');

    await undo(t, 2);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Delete Nodes with Eraser', async t => {
    await openFile(t, selectors.testEcore);
    await t
        .click(selectors.eraser)
        .click(nodesSelector.classNode, { offsetY: -40 })
        .click(selectors.eraser)
        .click(nodesSelector.abstractNode, { offsetY: -40 })
        .click(selectors.eraser)
        .click(nodesSelector.enumNode, { offsetY: -40 })
        .click(selectors.eraser)
        .click(nodesSelector.interfaceNode, { offsetY: -40 })
        .expect(nodesSelector.classNode.exists).notOk('Class has been deleted')
        .expect(nodesSelector.abstractNode.exists).notOk('Abstract has been deleted')
        .expect(nodesSelector.enumNode.exists).notOk('Enum has been deleted')
        .expect(nodesSelector.interfaceNode.exists).notOk('Interface has been deleted');

    await undo(t, 4);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Delete Nodes with DEL', async t => {
    await openFile(t, selectors.testEcore);
    await t.wait(100)
    await focus();
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
        .expect(nodesSelector.classNode.exists).notOk('Class has been deleted')
        .expect(nodesSelector.abstractNode.exists).notOk('Abstract has been deleted')
        .expect(nodesSelector.interfaceNode.exists).notOk('Interface has been deleted')
        .expect(nodesSelector.enumNode.exists).notOk('Enum has been deleted');

    await undo(t, 4);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Delete Edges', async t => {
    await openFile(t, selectors.testEcore);
    await t
        .click(selectors.eraser)
        .click(nodesSelector.classNode, { offsetX: 90 })
        .click(selectors.eraser)
        .click(nodesSelector.classNode, { offsetX: 6, offsetY: 185 })
        .click(selectors.eraser)
        .click(nodesSelector.interfaceNode, { offsetX: -150, offsetY: 12 })
        .expect(edgeSelector.referenceEdge.exists).notOk('Reference deleted')
        .expect(edgeSelector.containmentEdge.exists).notOk('Containment deleted')
        .expect(edgeSelector.inheritanceEdge.exists).notOk('Inheritance deleted');

    await undo(t, 3);
    await closeEditor(t);

}).after(checkDefaultWorkbench);

test('Delete Edges with DEL', async t => {
    await openFile(t, selectors.testEcore);
    await t.wait(100)
    await focus();
    await t
        .wait(200)
        .click(edgeSelector.referenceEdge)
        .wait(200)
        .pressKey('delete')
        .wait(200)
        .click(edgeSelector.containmentEdge)
        .wait(200)
        .pressKey('delete')
        // Skip this temporarily, as it tends to fail on the CI environment
        // .wait(500)
        // .click(edgeSelector.inheritanceEdge, { offsetX: -162 })
        // .wait(500)
        // .pressKey('delete')
        // .wait(500)
        .expect(edgeSelector.referenceEdge.exists).notOk('Reference deleted')
        .expect(edgeSelector.containmentEdge.exists).notOk('Containment deleted');
    // .expect(edgeSelector.inheritanceEdge.exists).notOk('Inheritance deleted');

    await undo(t, 2);
    await closeEditor(t);

}).after(checkDefaultWorkbench);
