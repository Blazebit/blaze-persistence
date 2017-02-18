## Contributing

Hello and thank you for considering to contribute to Blaze-Persistence!

### Questions or possible bugs

Always remember, there are no stupid questions, so don't hesitate to ask anything that comes to your mind.

If would you like to research a topic before discussing it, take a look at our [documentation](https://persistence.blazebit.com/documentation.html) or
[search the issue tracker](https://github.com/Blazebit/blaze-persistence/issues?q=something) for something similar.
You can also try to search on [Stack Overflow](http://stackoverflow.com/questions/tagged/blaze-persistence), but if all you want is just ask a quick question,
it's probably best to join us on Slack! [![Slack Status](https://blazebit.herokuapp.com/badge.svg)](https://blazebit.herokuapp.com) 

### Bugs

When you are confident that you have a unique bug that hasn't been [tracked](https://github.com/Blazebit/blaze-persistence/issues?q=something) yet,
we very much welcome if you could [create an issue](https://github.com/Blazebit/blaze-persistence/issues/new) with steps to reproduce the bug.
Ideally, you would create a pull request against the master branch that reproduces the issue within our testsuite,
but we are also happy if you create a pull request against our https://github.com/Blazebit/blaze-persistence-test-case-template[testcase template project] which is probably easier for beginners.

### Documentation and Website issues or typos

Just create a pull request with a simple comment containing what you fixed i.e. "Fixed website typo" or "Improved wording of documentation" and we will merge these fixes as soon as possible.

### Features

You have an idea for a feature? Then tell us about it by [creating an issue](https://github.com/Blazebit/blaze-persistence/issues/new).
As we try to react to new issues as fast as we can, you can expect to very quickly receive information about possible consideration of a feature.
If it's a small addition, it might very well be considered for one of the next releases, but since we have a rough [roadmap](https://github.com/Blazebit/blaze-persistence/blob/master/roadmap.asciidoc) for big ticket features,
we generally don't squeeze in bigger features into near future releases.
It might be a different story if you offer to work on the feature yourself, but since we have to review and integrate your work, it might still take some time.

### Contributing Code

Everyone can contribute! Please just make sure you let us know that you want to work on something before you actually do, so we can discuss how you could do it best and also avoid possible conflicts.

#### 1. Prepare environment

Before you start, make sure you create a branch based on the master branch on your [fork](https://help.github.com/articles/fork-a-repo) with a descriptive branch name that starts with the issue number.

```sh
git checkout -b 123-cool-feature
```

As you probably saw in the [README](https://github.com/Blazebit/blaze-persistence#setup-local-development), we use JDK 7+ and Maven for the build, so please make sure you have that installed.

In order to build and test Blaze-Persistence you should invoke

```sh
mvn clean install
```

Now you should be ready to get started within your favourite IDE.

#### 2. Implement a test

Before trying to change the implementation, you should make sure you have a test case that can reproduce or showcase a problem.
When the contribution is about a bug, you should create a separate test class for the issue i.e. `Issue123Test` in the testsuite project of the affected module.
If it is about a new feature, please try to find a more appropriate name for the test class or add your tests to an existing test class that is about related functionality.
If your test is Hibernate specific i.e. uses Hibernate specific features, please add it to the src/test/hibernate source tree.

Chances are high, that the testsuite already contains an entity model that fits your needs in a test, but if you need a very specific entity model for the test, create the classes in src/main/java of the testsuite project and add them to the src/main/resources/META-INF/persistence.xml so that JPA providers can enhance the classes.

Every test that extends `AbstractCoreTest` drops and then creates a database schema for the entity model as defined via `getEntityClasses()`, so you can assume a safe state. If you need data for your test, either create that in a `@Before` annotated setup method, or as part of your test by using the `transactional()` method.
In general, a good test case should first define the *given state* i.e. data or configuration, then execute code that should be tested and finally verify that the expected outcome matches the actual outcome.

After you verified that the test you wrote reproduces the actual problem you should commit just the test with a simple message containing the issue number.

```sh
git commit -m "Test for #123"
```

If all you wanted to do is create a test case for the issue, then all you are left to do is create a [pull request](#pull-request).

#### 3. Implement a fix

In general, the first thing you should do is analyze the problem. If the change required to fix the issue is rather small, just go ahead and try it out.
If a fix would require massive changes, you should first discuss the findings of your analysis and how you would fix the problem on the issue on GitHub.
The maintainers will tell you if the solution is appropriate or what you should do differntly. Maybe the massive changes aren't required at all!

Other than that, there is not much to say about how to fix an issue. Just make sure your code aligns with our checkstyle rules and everything else will be discussed on a case by case basis.

After you verified that the test you wrote before is fixed by the changes, you should run the whole testsuite with

```sh
mvn clean install
```

and finally commit the fix with a simple message containing the issue number.

```sh
git commit -m "Fix for #123"
```

#### <a name="pull-request"></a>4. Create a pull request

After all your work is committed, you should rebase your changes on the latest master state.
In order to do so, you should add the original repository as remote and checkout the latest changes from the master branch first.

```sh
git remote add upstream https://github.com/Blazebit/blaze-persistence.git
git checkout master
git pull upstream master
```

Then you rebase your branch on top of the latest master, and push it to your fork!

```sh
git checkout 123-cool-feature
git rebase master
git push --set-upstream origin 123-cool-feature
```

Now you can finally go to GitHub and [create a pull request](https://help.github.com/articles/creating-a-pull-request).

#### 5. Update a Pull Request

There are multiple situations why it might be required to update a pull request.
The most common requests of maintainers are

##### Rebase the PR

If the state against which you did your changes and the actual master state diverge, it might be necessary to rebase your changes again to make merging easier.
Doing a rebase involves the following steps:

```sh
git checkout 123-cool-feature
git pull --rebase upstream master
git push --force-with-lease 123-cool-feature
```

##### Fix issues

If you introduced checkstyle issues or problems with a specific DBMS it might be necessary to do some changes.
After you fixed the issues, you should add the changes to your last commit and push them to your branch:

```sh
git commit --amend
git push --force-with-lease 123-cool-feature
```

#### 6. Merge a PR (maintainers only)

A PR can only be merged into master by a maintainer if:

* It tests are passing CI.
* It has been approved by at least two maintainers. If it was a maintainer who
  opened the PR, only one extra approval is needed.
* It has no requested changes.
* It is up to date with current master.

Any maintainer is allowed to merge a PR if all of these conditions are
met.