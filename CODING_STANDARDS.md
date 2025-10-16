# [Contracts uses Googles Style Guide](https://google.github.io/styleguide/javaguide.html)

## With the following deviations
### Arrangement:
#### 1. The most accessible stuff is at the top.
    @Override
    public void doSomePublicallyDelaredAction() {
    }
#### 2. When possible all code is ordered by accessibility.
    public
    protected 
    package
    private
#### 3. Public static final fields at the top.
    public static final String APPLICATION_NAME = "MyApp";
#### 4. All other fields should be private and at the bottom. Create accessors methods instead.
    // the last thing someone needs to know are the 
    // implementation fields used to implement a feature

    private final CountDownLatch thresholdLatch;
    private final Map<String,String> cache;
#### 5. If Java allowed imports at the bottom, I would require it :)