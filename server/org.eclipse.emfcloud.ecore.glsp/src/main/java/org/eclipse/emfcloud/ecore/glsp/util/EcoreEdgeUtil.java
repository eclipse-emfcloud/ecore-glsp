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
package org.eclipse.emfcloud.ecore.glsp.util;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.emfcloud.ecore.enotation.Dimension;
import org.eclipse.emfcloud.ecore.enotation.EnotationFactory;
import org.eclipse.emfcloud.ecore.enotation.Point;
import org.eclipse.glsp.graph.GDimension;
import org.eclipse.glsp.graph.GPoint;
import org.eclipse.glsp.graph.GraphFactory;

public class EcoreEdgeUtil {
	public static String getStringId(EReference reference) {
		return Integer.toString(reference.hashCode());
	}

	/**
	 * Retrieves the Edge Id, who is associated with this label.
	 */
	public static String getEdgeId(String labelId) {
		return labelId.split("_")[0];
	}

	public static Dimension dimension(double height, double width) {
		Dimension dimension = EnotationFactory.eINSTANCE.createDimension();
		dimension.setHeight(height);
		dimension.setWidth(width);
		return dimension;
	}

	public static Dimension copy(GDimension toCopy) {
		return dimension(toCopy.getWidth(), toCopy.getHeight());
	}
	
	public static GDimension gdimension(double width, double height) {
	      GDimension dimension = GraphFactory.eINSTANCE.createGDimension();
	      dimension.setWidth(width);
	      dimension.setHeight(height);
	      return dimension;
	   }

	   public static GDimension copy(Dimension toCopy) {
	      return gdimension(toCopy.getWidth(), toCopy.getHeight());
	   }

	public static Point point(double x, double y) {
		Point point = EnotationFactory.eINSTANCE.createPoint();
		point.setX(x);
		point.setY(y);
		return point;
	}

	public static Point copy(GPoint toCopy) {
		return point(toCopy.getX(), toCopy.getY());
	}
	
	public static GPoint gpoint(double x, double y) {
	      GPoint point = GraphFactory.eINSTANCE.createGPoint();
	      point.setX(x);
	      point.setY(y);
	      return point;
	   }

	   public static GPoint copy(Point toCopy) {
	      return gpoint(toCopy.getX(), toCopy.getY());
	   }

}