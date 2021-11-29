class SymbolDetails {
    private String name;
    private String type;
    private String kind;
    private int count;


    SymbolDetails(String name, String type, String kind, int count) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.count = count;
    }

    @Override
    public String toString() {
        return "SymbolDetails{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", kind='" + kind + '\'' +
                ", count=" + count +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
