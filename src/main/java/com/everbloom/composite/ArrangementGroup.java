package com.everbloom.composite;

import java.util.ArrayList;
import java.util.List;

public class ArrangementGroup implements EventPackageComponent {
    private final String name;
    private final List<EventPackageComponent> children = new ArrayList<>();
    public ArrangementGroup(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Group name is required.");
        this.name = name;
    }
    public void add(EventPackageComponent component) {
        if (component == null) throw new IllegalArgumentException("Arrangement component is required.");
        children.add(component);
    }
    public List<EventPackageComponent> getChildren() { return List.copyOf(children); }
    public List<ArrangementGroup> getAllGroups() {
        List<ArrangementGroup> groups = new ArrayList<>();
        groups.add(this);
        for (EventPackageComponent child : children) {
            if (child instanceof ArrangementGroup group) groups.addAll(group.getAllGroups());
        }
        return List.copyOf(groups);
    }
    public String getName() { return name; }
    public long getTotalPrice() { long total = 0; for (EventPackageComponent child : children) total += child.getTotalPrice(); return total; }
    public String getSummary() { StringBuilder summary = new StringBuilder(name).append("\n"); appendChildren(summary, "", children); return summary.toString().trim(); }
    private void appendChildren(StringBuilder summary, String indent, List<EventPackageComponent> components) {
        for (EventPackageComponent component : components) {
            summary.append(indent).append("- ").append(component.getName()).append(": BDT ").append(component.getTotalPrice()).append("\n");
            if (component instanceof ArrangementGroup group) appendChildren(summary, indent + "  ", group.children);
        }
    }
    @Override public String toString() { return name; }
}
