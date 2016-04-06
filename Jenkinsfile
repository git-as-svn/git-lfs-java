node {
  stage 'Checkout'
  checkout ([
    $class: 'GitSCM',
    userRemoteConfigs: [[url: 'https://github.com/bozaro/git-lfs-java.git']],
    extensions: [[$class: 'CleanCheckout']]
  ])

  stage 'Build'
  sh "./gradlew assemble"

  stage 'Test'
  sh "./gradlew check -PtestIgnoreFailures=true"
  step ([
    $class: 'JUnitResultArchiver',
    testResults: '**/build/test-results/*.xml'
  ])
}
