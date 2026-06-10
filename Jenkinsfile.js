pipeline{
    agent any
    tools{
        maven 'mvn_3.9'
    }
    environment{
        deploy_DB= "true"
        DB_HOST   = "18.219.202.35"
        APP_HOST  = "18.117.220.216"
    }
    stages{
        stage("Github Checkout"){
            steps{
                git branch: 'main', url: 'https://github.com/eanyia/acada-voteapp.git'
            }
        }
        stage("Maven Packaging"){
            steps{
                // sh 'docker run --rm -v ./:/tmp/acada-voteapp -w /tmp/acada/webapp maven:latest mvn clean package && echo "Build Successful"'
                sh 'mvn clean package'
            }
        }
        stage("Image Build and Push"){
            steps{
               withCredentials([usernamePassword(credentialsId: 'ea227241-287a-4d00-9ec1-41624eb76aed', passwordVariable: 'DOCKERHUB_PASS', usernameVariable: 'DOCKERHUB_USER')]) {
                   sh 'echo ${DOCKERHUB_PASS} | docker login -u ${DOCKERHUB_USER} --password-stdin'
                }
                sh 'docker build -t eanyia/acada-voteapp:Release10.0 .'
                sh 'docker push eanyia/acada-voteapp:Release10.0'
            }
        }
        stage("Deploy: DB"){
            steps{
                script{
                    if(deploy_DB == "true"){
                        withCredentials([usernamePassword(credentialsId: 'DBV-Password', passwordVariable: 'POSTGRES_PASS', usernameVariable: 'POSTGRES_USER')]) {
                            sh """
                            echo 'POSTGRES_USER=${POSTGRES_USER}'      > .env
                            echo 'POSTGRES_PASSWORD=${POSTGRES_PASS}' >> .env
                            """
                        }
                        withCredentials([sshUserPrivateKey(credentialsId: 'DBV-HOST', keyFileVariable: 'DB_SSH_KEY', usernameVariable: 'DB_SSH_USER')]) {
                            sh "ssh -o StrictHostKeyChecking=no -i ${DB_SSH_KEY} ${DB_SSH_USER}@${DB_HOST} 'mkdir -p ~/vote-app-db/'"
                            sh "scp -o StrictHostKeyChecking=no -i ${DB_SSH_KEY} .env V1__init.sql ${DB_SSH_USER}@${DB_HOST}:~/vote-app-db/"
                        sh """
                          ssh -o StrictHostKeyChecking=no -i ${DB_SSH_KEY} ${DB_SSH_USER}@${DB_HOST} '
                            docker rm -f naijavote-postgres || true
                            docker run -d \\
                              --name naijavote-postgres \\
                              -p 5432:5432 \\
                              -v ~/vote-app-db/V1__init.sql:/docker-entrypoint-initdb.d/V1__init.sql \\
                              --env-file ~/vote-app-db/.env \\
                              postgres:16-alpine
                           '
                         """
                        }
                    }
                }
            }

        }
        stage("Deploy App"){
            steps{
                withCredentials([
                    usernamePassword(credentialsId: 'ea227241-287a-4d00-9ec1-41624eb76aed', passwordVariable: 'DOCKERHUB_PASS', usernameVariable: 'DOCKERHUB_USER'),
                    usernamePassword(credentialsId: 'DB-Password', passwordVariable: 'POSTGRES_PASS', usernameVariable: 'POSTGRES_USER')
                    ]){
                      sh """
                    echo 'DB_HOST=${DB_HOST}'           > .env
                    echo 'DB_PORT=5432'                >> .env
                    echo 'DB_NAME=acadavote_db'            >> .env
                    echo 'DB_USERNAME=${POSTGRES_USER}' >> .env
                    echo 'DB_PASSWORD=${POSTGRES_PASS}' >> .env
                      """
                      withCredentials([sshUserPrivateKey(credentialsId: 'DBV-HOST', keyFileVariable: 'APP_SSH_KEY', usernameVariable: 'APP_SSH_USER')]) {
                        sh "ssh -o StrictHostKeyChecking=no -i ${APP_SSH_KEY} ${APP_SSH_USER}@${APP_HOST} 'mkdir -p ~/vote-app/'"
                        sh "scp -o StrictHostKeyChecking=no -i ${APP_SSH_KEY} .env ${APP_SSH_USER}@${APP_HOST}:~/vote-app/"
                        sh """
                          ssh -o StrictHostKeyChecking=no -i ${APP_SSH_KEY} ${APP_SSH_USER}@${APP_HOST} \
                            'echo ${DOCKERHUB_PASS} | docker login -u ${DOCKERHUB_USER} --password-stdin && \
                            docker rm -f acada-voteapp || true && \
                            docker pull eanyia/acada-voteapp:Release10.0 && \
                            docker run -d \\
                              --name acada-voteapp \\
                              -p 8080:8080 \\
                              --env-file ~/vote-app/.env \\
                              eanyia/acada-voteapp:Release10.0'
                        """
                    }
                }
            }
        }
    }
}