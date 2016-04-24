package net.java.textilej.parser.outline;

import java.util.ArrayList;
import java.util.List;

public class OutlineItem {

	private final OutlineItem parent;
	private final int level;
	private final List<OutlineItem> children = new ArrayList<OutlineItem>();
	private final int offset;
	private final int length;
	private final String id;
	private String label;
	private String kind;
	private int childOffset;
	private String tooltip;
	
	public OutlineItem(OutlineItem parent,int level,String id, int offset, int length, String label) {
		super();
		this.parent = parent;
		this.level = (parent == null)?0:level;
		if (parent != null && level < parent.getLevel()) {
			throw new IllegalArgumentException();
		}
		this.id = id;
		this.offset = offset;
		this.length = length;
		this.label = label;
		if (parent != null) {
			parent.addChild(this);
		}
	}
	
	public int getLength() {
		return length;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getLabel() {
		return label;
	}
	
	public String getId() {
		return id;
	}

	public int getLevel() {
		if (parent == null) {
			return 0;
		}
		return level;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public OutlineItem getParent() {
		return parent;
	}

	public List<OutlineItem> getChildren() {
		return children;
	}

	public int getOffset() {
		return offset;
	}

	@Override
	public int hashCode() {
		return calculatePositionKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final OutlineItem other = (OutlineItem) obj;
		return other.calculatePositionKey().equals(calculatePositionKey());
	}

	public void clear() {
		children.clear();
	}
	
	private String calculatePositionKey() {
		if (parent == null) {
			return "";
		}
		return getParent().calculatePositionKey()+"/"+kind+childOffset;
	}

	private void addChild(OutlineItem outlineItem) {
		outlineItem.childOffset = children.size();
		children.add(outlineItem);
	}
	
	public OutlineItem findNearestMatchingOffset(int offset) {
		NearestItemVisitor visitor = new NearestItemVisitor(offset);
		accept(visitor);
		
		return visitor.nearest;
	}
	
	private static class NearestItemVisitor implements Visitor {

		private OutlineItem nearest = null;
		private int offset;
		
		public NearestItemVisitor(int offset) {
			this.offset = offset;
		}

		public boolean visit(OutlineItem item) {
			if (item.getOffset() == -1) {
				return true;
			}
			if (nearest == null) {
				nearest = item;
				return true;
			}
			int itemDistance = item.distance(offset);
			if (itemDistance > 0) {
				return true;
			}
			int nearestDistance = nearest.distance(offset);
			nearestDistance = Math.abs(nearestDistance);
			itemDistance = Math.abs(itemDistance);
			if (itemDistance < nearestDistance) {
				nearest = item;
			} else if (itemDistance > nearestDistance) {
				return false;
			}
			return true;
		}
		
	}
	
	public int distance(int offset) {
		int startDistance = this.offset-offset;
		
		return startDistance;
	}
	
	public interface Visitor {
		public boolean visit(OutlineItem item);
	}
	
	public void accept(Visitor visitor) {
		if (visitor.visit(this)) {
			for (OutlineItem item: getChildren()) {
				item.accept(visitor);
			}
		}
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public String getTooltip() {
		return tooltip;
	}
	
	
}
