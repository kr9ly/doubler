git clone "https://github.com/kr9ly/maven" /tmp/maven
./gradlew uploadArchives
cd /tmp/maven
if [[ $(git status --porcelain) ]];
then
	echo "start deploying.";
    git add -A .
    git config user.name ${GIT_USER_NAME}
    git config user.email ${GIT_USER_EMAIL}
    git commit -m 'Commit from CircleCI'
    git push origin gh-pages
else
    echo "nothing to commit.";
fi