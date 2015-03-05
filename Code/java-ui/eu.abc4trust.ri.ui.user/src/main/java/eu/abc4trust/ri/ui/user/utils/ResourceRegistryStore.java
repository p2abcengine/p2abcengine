//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ri.ui.user.utils;

import java.net.URL;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.rwt.SessionSingletonBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.abc4trust.ri.ui.user.Activator;

/**
 * Singleton store for resources (images, colors).<br/>
 * <br/>
 * Note that this class is implemented as <a href=http://wiki.eclipse.org/RAP/FAQ#What_is_a_Session_Singleton_and_how_can_I_implement_one.3F">Session Singleton</a>.
 * This was done because without the session singleton approach the images in the UI were not displayed any more
 * after restarting the application/UI, i.e., they were only displayed the first time the application/UI was started.
 * Apparently, when restarting the application, the ImageRegistry disposed the images which lead to this unintended behavior.
 */
public class ResourceRegistryStore {
	
	// Image constants
	public static final String IMG_ARROW			 							= "arrow"; //$NON-NLS-1$
	public static final String IMG_MISSING			 							= "missingImage"; //$NON-NLS-1$
	public static final String IMG_LOCK_SMALL			 						= "lock_small"; //$NON-NLS-1$
	public static final String IMG_INFORMATION			 						= "information"; //$NON-NLS-1$
	public static final String IMG_DELETE                                       = "delete"; //$NON-NLS-1$
	public static final String IMG_CREDENTIAL_DELETE_16x16                      = "credential_delete_16x16"; //$NON-NLS-1$
	public static final String IMG_CREDENTIAL_DELETE_64x64                      = "credential_delete_64x64"; //$NON-NLS-1$
	public static final String IMG_CHECKBOX_SELECTED                            = "checkbox_selected"; //$NON-NLS-1$
	public static final String IMG_CHECKBOX_UNSELECTED                          = "checkbox_unselected"; //$NON-NLS-1$
	
	// Color constants
	public static final String COL_GREY											= "col190190190"; //$NON-NLS-1$
	public static final String COL_BLUE001										= "blue001"; //$NON-NLS-1$
	public static final String COL_BLUE002										= "blue002"; //$NON-NLS-1$
	public static final String COL_RED001                                       = "red001"; //$NON-NLS-1$
	public static final String COL_GREEN001                                     = "green001"; //$NON-NLS-1$
	
	// Registries
	private final ImageRegistry imageRegistry;
	private final ColorRegistry colorRegistry;
	
	private ResourceRegistryStore() { // 'private' modifier prevents instantiation from outside
		imageRegistry = new ImageRegistry();
		colorRegistry = new ColorRegistry();
		initializeImages();
		initializeColors();
	}
	
	private void initializeImages() {
		imageRegistry.put(IMG_ARROW, getImageDescriptorFromPlugin("icons/arrow.png")); //$NON-NLS-1$
		imageRegistry.put(IMG_MISSING, getImageDescriptorFromPlugin("icons/missing.png")); //$NON-NLS-1$
		imageRegistry.put(IMG_LOCK_SMALL, getImageDescriptorFromPlugin("org.eclipse.rap.ui", "icons/full/progress/lockedstate.gif")); //$NON-NLS-1$ //$NON-NLS-2$
		imageRegistry.put(IMG_INFORMATION, getImageDescriptorFromPlugin("org.eclipse.rap.rwt", "resource/widget/rap/dialog/information.gif")); //$NON-NLS-1$ //$NON-NLS-2$
		imageRegistry.put(IMG_DELETE, getImageDescriptorFromPlugin("org.eclipse.rap.ui", "icons/full/etool16/delete.gif")); //$NON-NLS-1$ //$NON-NLS-2$
		imageRegistry.put(IMG_CREDENTIAL_DELETE_64x64, getImageDescriptorFromPlugin("icons/credential_delete_64x64.png")); //$NON-NLS-1$ //$NON-NLS-2$
		imageRegistry.put(IMG_CREDENTIAL_DELETE_16x16, getImageDescriptorFromPlugin("icons/credential_delete_16x16.png")); //$NON-NLS-1$ //$NON-NLS-2$
		imageRegistry.put(IMG_CHECKBOX_SELECTED, getImageDescriptorFromPlugin("org.eclipse.rap.rwt", "resource/widget/rap/button/check-selected.png")); //$NON-NLS-1$ //$NON-NLS-2$
		imageRegistry.put(IMG_CHECKBOX_UNSELECTED, getImageDescriptorFromPlugin("org.eclipse.rap.rwt", "resource/widget/rap/button/check-unselected.png")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private void initializeColors() {
		colorRegistry.put(COL_GREY, Display.getDefault().getSystemColor(SWT.COLOR_GRAY).getRGB());
		colorRegistry.put(COL_BLUE001, new RGB(167, 211, 233));
		colorRegistry.put(COL_BLUE002, new RGB(167, 211, 233));
		colorRegistry.put(COL_RED001, new RGB(220, 0, 0));
		colorRegistry.put(COL_GREEN001, new RGB(160, 255, 160));
	}
	
	private static ImageDescriptor getImageDescriptorFromPlugin(String imageFilePath) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, imageFilePath);
	}
	
	private static ImageDescriptor getImageDescriptorFromPlugin(String pluginID, String imageFilePath) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, imageFilePath);
	}
	
	public static ResourceRegistryStore getInstance() {
		return (ResourceRegistryStore) SessionSingletonBase.getInstance(ResourceRegistryStore.class);
	}
	
	public static ImageRegistry getImageRegistry() {
		return getInstance().imageRegistry;
	}
	
	public static ColorRegistry getColorRegistry() {
		return getInstance().colorRegistry;
	}
	
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}
	
	public static ImageDescriptor getImageDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}
	
	public static Color getColor(String key) {
		return getColorRegistry().get(key);
	}
	
	/**
	 * Generates a thumbnail from the image with the given URL.
	 * Thumbnails are cached.
	 * 
	 * @param image
	 * @param maxSideLength
	 * @return
	 */
	public static Image getThumbnail(URL image, int maxSideLength) {
		return getThumbnail(image, maxSideLength, false);
	}
	
	@Deprecated // TODO grey thumbnails not implemented yet
	public static Image getGrayThumbnail(URL image, int maxSideLength) {
		return getThumbnail(image, maxSideLength, true);
	}
	
	private static Image getThumbnail(URL image, int maxSideLength, boolean gray) {
		String id = "thumbmail::"+maxSideLength+"::"+image+"::"+gray; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		// check whether there is already a thumbnail for this image URL in the registry
		Image cachedThumbnail = ResourceRegistryStore.getImage(id);
		if (cachedThumbnail != null) {
			return cachedThumbnail;
		}
		
		ImageDescriptor imgDesc = ImageDescriptor.createFromURL(image);
		if (imgDesc == null) return getImage(IMG_MISSING);
		ImageData imgData = imgDesc.getImageData();
		if (imgData == null) return getImage(IMG_MISSING);
		
		int oWidth = imgData.width;
		int oHeight = imgData.height;
		
		double ratio;
		if (oWidth >= oHeight) {
			ratio = (double)maxSideLength/oWidth;
		} else {
			ratio = (double)maxSideLength/oHeight;
		}
		
		Image thumbnail = new Image(Display.getDefault(), imgData.scaledTo((int)(oWidth*ratio), (int)(oHeight*ratio)));
//		if (gray) { // TODO add support for gray image or remove gray parameter
//			Image grayed = new Image(Display.getDefault(), thumbnail, SWT.image);
//			thumbnail.dispose();
//			// put into registry
//		}
		ResourceRegistryStore.getImageRegistry().put(id, thumbnail);
		return thumbnail;
	}
}
