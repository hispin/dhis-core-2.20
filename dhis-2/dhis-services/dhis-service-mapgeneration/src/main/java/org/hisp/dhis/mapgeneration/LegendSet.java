package org.hisp.dhis.mapgeneration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

/**
 * This class can be used to render a set of legends onto one image.
 * 
 * @author Kristin Simonsen <krissimo@ifi.uio.no>
 * @author Kjetil Andresen <kjetil.andrese@gmail.com>
 */
public class LegendSet {

	private List<Legend> legends;
	private Color backgroundColor = null;

	private static final int LEGEND_WIDTH = 250;
	private static final int LEGEND_MARGIN_LEFT = 5;
	private static final int LEGEND_MARGIN_BOTTOM = 20;

	public LegendSet() {
		legends = new LinkedList<Legend>();
	}

	public LegendSet(InternalMapLayer mapLayer) {
		legends = new LinkedList<Legend>();
		addMapLayer(mapLayer);
	}

	public LegendSet(List<InternalMapLayer> mapLayers) {
		legends = new LinkedList<Legend>();
		addMapLayers(mapLayers);
	}

	/**
	 * Render the legends contained in this set onto a image. The width of the
	 * image returned may vary, depending on how many columns of legends that is
	 * added. The image height can be decided by the user, but if the biggest
	 * legend is higher than imageMaxHeight, the height will automatically be
	 * set to the height of this legend.
	 * 
	 * @param imageMaxHeight
	 * @return
	 */
	public BufferedImage render(int imageMaxHeight) {
		Dimension imageDimensions = calculateImageWidthAndHeight(imageMaxHeight);
		int imageWidth = (int) imageDimensions.getWidth();
		int imageHeight = (int) imageDimensions.getHeight();
		BufferedImage image = new BufferedImage(imageWidth, imageHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) image.getGraphics();

		// Overwrite if one of the legends is bigger than imageMaxHeight
		if (imageDimensions.getHeight() > imageMaxHeight) {
			imageMaxHeight = (int) imageDimensions.getHeight();
		}

		// Draw a background if the background color is specified
        // NOTE It will be transparent otherwise, which is desired
		if (backgroundColor != null) {
        	g.setColor(backgroundColor);
        	g.fill(new Rectangle(0, 0, imageWidth, imageHeight));
		}

		// Turn anti-aliasing on
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		int y = 0;
		int col = 0;
		AffineTransform orginalTransform = g.getTransform();

		g.translate(LEGEND_MARGIN_LEFT, 0);

		// Draw legends
		for (Legend legend : legends) {
			if (y + legend.getHeight() >= imageMaxHeight) {
				col++;
				y = 0;
				g.setTransform(orginalTransform);
				g.translate(col * LEGEND_WIDTH, 0);
			}

			legend.draw(g);
			g.translate(0, LEGEND_MARGIN_BOTTOM);

			y += legend.getHeight() + LEGEND_MARGIN_BOTTOM;
		}

		return image;
	}

	public void addLegend(Legend legend) {
		legends.add(legend);
	}

	public void addLegends(List<Legend> legends) {
		for (Legend legend : legends) {
			addLegend(legend);
		}
	}

	public void addMapLayer(InternalMapLayer mapLayer) {
		legends.add(new Legend(mapLayer));
	}

	public void addMapLayers(List<InternalMapLayer> mapLayers) {
		for (InternalMapLayer mapLayer : mapLayers) {
			addMapLayer(mapLayer);
		}
	}

	public List<Legend> getLegends() {
		return legends;
	}

	public Color getBackground() {
		return backgroundColor;
	}

	public void setBackground(Color c) {
		backgroundColor = c;
	}

	private Dimension calculateImageWidthAndHeight(int maxImageHeight) {
		int imageWidth = LEGEND_WIDTH;
		int imageHeight = maxImageHeight;

		// Ensure that every legend fits the maxImageHeight
		for (Legend legend : legends) {
			if (legend.getHeight() + LEGEND_MARGIN_BOTTOM > imageHeight) {
				imageHeight = legend.getHeight() + LEGEND_MARGIN_BOTTOM;
			}
		}

		int y = 0;

		// Calculate image width
		for (Legend legend : legends) {
			if (legend.getHeight() + LEGEND_MARGIN_BOTTOM + y >= imageHeight) {
				imageWidth += LEGEND_WIDTH;
				y = 0;
			}

			y += legend.getHeight() + LEGEND_MARGIN_BOTTOM;
		}

		return new Dimension(imageWidth, imageHeight);
	}
}
