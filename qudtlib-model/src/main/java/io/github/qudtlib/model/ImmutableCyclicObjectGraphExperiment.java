package io.github.qudtlib.model;

public class ImmutableCyclicObjectGraphExperiment {

    static class Parent {
        final Child child;

        final String name;

        Parent(ParentBuilder builder, ChildBuilder childBuilder, String name) {
            builder.setInstance(this);
            this.child = childBuilder.build();
            this.name = name;
        }

        public String toString() {
            return name + " is the parent of " + this.child.name;
        }
    }

    static class Child {
        final Parent parent;

        final String name;

        Child(ChildBuilder builder, ParentBuilder parentBuilder, String name) {
            builder.setInstance(this);
            this.parent = parentBuilder.build();
            this.name = name;
        }

        public String toString() {
            return name + " is the child of " + this.parent.name;
        }
    }

    static class ParentBuilder {
        ChildBuilder childBuilder;
        String name;
        Parent instance = null;

        public ParentBuilder() {}

        void setInstance(Parent instance) {
            this.instance = instance;
        }

        Parent build() {
            if (this.instance == null) {
                this.instance = new Parent(this, this.childBuilder, this.name);
            }
            return this.instance;
        }

        public ParentBuilder child(ChildBuilder childBuilder) {
            this.childBuilder = childBuilder;
            return this;
        }

        public ParentBuilder name(String name) {
            this.name = name;
            return this;
        }
    }

    static class ChildBuilder {
        ParentBuilder parentBuilder;
        String name;
        Child instance = null;

        Child build() {
            if (this.instance == null) {
                this.instance = new Child(this, parentBuilder, name);
            }
            return this.instance;
        }

        void setInstance(Child instance) {
            this.instance = instance;
        }

        public ChildBuilder parent(ParentBuilder parentBuilder) {
            this.parentBuilder = parentBuilder;
            return this;
        }

        public ChildBuilder name(String name) {
            this.name = name;
            return this;
        }
    }

    public static void main(String[] args) {
        ParentBuilder pb = new ParentBuilder();
        ChildBuilder cb = new ChildBuilder();
        pb.name("Anakin").child(cb);
        cb.name("Luke").parent(pb);
        Parent p = pb.build();
        Child c = cb.build();
        System.out.println(p);
        System.out.println(c);
    }
}
