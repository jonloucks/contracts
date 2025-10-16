# [Contracts uses Googles Style Guide](https://google.github.io/styleguide/javaguide.html)

## with the following deviations
1. Arrangement matters. The most accessible stuff is at the top.
2. When possible all code is ordered by accessibility. public -> protected -> package -> private
2. Public static final fields at the top.
3. All other fields should be private and at the bottom. Create accessors methods instead.
4. If Java allowed imports at the bottom, I would require it :)