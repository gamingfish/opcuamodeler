package me.steffenjacobs.opcuadisplay.views.explorer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import me.steffenjacobs.opcuadisplay.Activator;
import me.steffenjacobs.opcuadisplay.dialogs.ShowNodeDialog;
import me.steffenjacobs.opcuadisplay.shared.domain.CachedBaseNode;
import me.steffenjacobs.opcuadisplay.shared.util.EventBus;
import me.steffenjacobs.opcuadisplay.shared.util.Images;
import me.steffenjacobs.opcuadisplay.shared.util.EventBus.Event;
import me.steffenjacobs.opcuadisplay.shared.util.EventBus.EventListener;
import me.steffenjacobs.opcuadisplay.views.attribute.events.AttributeModifiedEvent;
import me.steffenjacobs.opcuadisplay.views.explorer.dialogs.LoadVariablesDialog;
import me.steffenjacobs.opcuadisplay.views.explorer.events.SelectedNodeChangedEvent;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class OpcUaExplorerView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "me.steffenjacobs.opcuadisplay.views.explorer.OpcUaExplorerView";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action doubleClickAction, selectionChangedAction;
	private Action openLoadVariablesView;
	private Action collapseAllAction, expandAllAction;
	private Action addVariable, addMethod, addObject, addProperty, addObjectType, addVariableType, addDataType;
	private OpcUaConnector connector;

	/**
	 * The constructor.
	 */
	public OpcUaExplorerView() {
	}

	private void hookDoubleClickAction() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				selectionChangedAction.run();

			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);

		connector = new OpcUaConnector(this.viewer.getControl().getShell());
		connector.overwriteRoot(CachedBaseNode.getDummyNoData());
		viewer.setContentProvider(connector);
		viewer.setInput(getViewSite());
		viewer.setLabelProvider(new NodeClassLabelProvider());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "me.steffenjacobs.opcuadisplay.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		registerListeners();
	}

	private void registerListeners() {
		EventBus.getInstance().addListener(AttributeModifiedEvent.IDENTIFIER, new EventListener<EventBus.Event>() {
			@Override
			public void onAction(Event event) {
				connector.overwriteRoot(connector.getRoot());
				viewer.refresh();
			}
		});
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				OpcUaExplorerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(openLoadVariablesView);
		manager.add(new Separator());
		manager.add(collapseAllAction);
		manager.add(expandAllAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(openLoadVariablesView);
		manager.add(new Separator());
		manager.add(collapseAllAction);
		manager.add(expandAllAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(openLoadVariablesView);
		manager.add(new Separator());
		manager.add(collapseAllAction);
		manager.add(expandAllAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void handleLoadVariableAction() {
		LoadVariablesDialog dialog = new LoadVariablesDialog(new Shell());
		dialog.create();

		CachedBaseNode root = connector.getRoot();
		connector.overwriteRoot(CachedBaseNode.getDummyLoading());
		viewer.refresh();

		int result = dialog.open();
		if (result == Window.OK) {
			EventBus.getInstance().fireEvent(new SelectedNodeChangedEvent(null));
			connector.loadVariables(dialog.getURL());
			viewer.refresh();
			expandToDefaultState();
		} else {
			connector.overwriteRoot(root);
			viewer.refresh();
			expandToDefaultState();
		}
	}

	private void expandToDefaultState() {
		viewer.setExpandedElements(connector.getRoot().getChildren());
		viewer.setExpandedState(connector.getRoot(), true);
	}

	private void handleDoubleClickAction() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj instanceof CachedBaseNode) {
			if (((CachedBaseNode) obj).isDummy()) {
				handleLoadVariableAction();
			} else {
				EventBus.getInstance().fireEvent(new SelectedNodeChangedEvent((CachedBaseNode) obj));
				ShowNodeDialog dialog = new ShowNodeDialog(viewer.getControl().getShell());
				dialog.create();
				dialog.setDisplayedNode((CachedBaseNode) obj);
				if (dialog.open() == Window.OK) {
					viewer.refresh();
				}
			}
		}
	}

	/** called, when the selection changed. Updates the attribute view. */
	private void handleSelectionChangedAction() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj instanceof CachedBaseNode) {
			if (!((CachedBaseNode) obj).isDummy()) {
				EventBus.getInstance().fireEvent(new SelectedNodeChangedEvent((CachedBaseNode) obj));
			}
		}
	}

	private void makeEditActions() {
		// add variable action
		addVariable = new Action() {
			public void run() {
			}
		};
		addVariable.setText("Add Variable");
		addVariable.setToolTipText("Add Variable");
		addVariable.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));

		// add method action
		addMethod = new Action() {
			public void run() {
			}
		};
		addMethod.setText("Add Method");
		addMethod.setToolTipText("Add Method");
		addMethod.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));

		// add object action
		addObject = new Action() {
			public void run() {
			}
		};
		addObject.setText("Add Object");
		addObject.setToolTipText("Add Object");
		addObject.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));

		// add Property action
		addProperty = new Action() {
			public void run() {
			}
		};
		addProperty.setText("Add Property");
		addProperty.setToolTipText("Add Property");
		addProperty.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));

		// add ObjectType action
		addObjectType = new Action() {
			public void run() {
			}
		};
		addObjectType.setText("Add ObjectType");
		addObjectType.setToolTipText("Add ObjectType");
		addObjectType.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));

		// add VariableType action
		addVariableType = new Action() {
			public void run() {
			}
		};
		addVariableType.setText("Add VariableType");
		addVariableType.setToolTipText("Add VariableType");
		addVariableType.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));

		// add DataType action
		addDataType = new Action() {
			public void run() {
			}
		};
		addDataType.setText("Add DataType");
		addDataType.setToolTipText("Add DataType");
		addDataType.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
	}

	private void makeActions() {
		makeEditActions();

		// open load variables view
		openLoadVariablesView = new Action() {
			public void run() {
				handleLoadVariableAction();
			}
		};
		openLoadVariablesView.setText("Load Variables...");
		openLoadVariablesView.setToolTipText("Load Variables...");
		openLoadVariablesView.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));

		// double click action
		doubleClickAction = new Action() {
			public void run() {
				handleDoubleClickAction();
			}
		};

		// click action
		selectionChangedAction = new Action() {
			public void run() {
				handleSelectionChangedAction();
			}
		};

		// collapse all action
		collapseAllAction = new Action() {
			public void run() {
				viewer.collapseAll();
				expandToDefaultState();
			}
		};
		collapseAllAction.setText("Collapse All");
		collapseAllAction.setToolTipText("Collapse All");
		collapseAllAction.setImageDescriptor(Activator.getImageDescriptor(Images.IMG_COLLAPSE_ALL.getIdentifier()));

		// expand all action
		expandAllAction = new Action() {
			public void run() {
				viewer.expandAll();
			}
		};
		expandAllAction.setText("Expand All");
		expandAllAction.setToolTipText("Expand All");
		expandAllAction.setImageDescriptor(Activator.getImageDescriptor(Images.IMG_EXPAND_ALL.getIdentifier()));
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
