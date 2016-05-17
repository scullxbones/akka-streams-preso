module.exports = (grunt) ->

    dir_config =
      root: 'src/main/webapp/'
      dist: 'target/dist/'
      webapp: 'target/webapp/'

    grunt.initConfig
        pkg: grunt.file.readJSON 'package.json'

        dirs: dir_config

        watch:
            livereload:
                options:
                    livereload: true
                files: [
                    '<%= dirs.root %>/index.html'
                    '<%= dirs.root %>/slides/{,*/}*.{md,html}'
                    '<%= dirs.root %>/javascripts/*.js'
                    '<%= dirs.root %>/stylesheets/*.css'
                    '<%= dirs.root %>/resources/**'
                ]

            static:
                files: [
                    '<%= dirs.root %>/**/*.*'
                ]
                tasks: ['copy:webapp']

            index:
                files: [
                    '<%= dirs.root %>/templates/_index.html'
                    '<%= dirs.root %>/templates/_section.html'
                    '<%= dirs.root %>/slides/list.json'
                ]
                tasks: ['buildIndex']

            coffeelint:
                files: [
                  'Gruntfile.coffee'
                ]
                tasks: ['coffeelint']

            jshint:
                files: [
                  '<%= dirs.root %>/js/*.js'
                ]
                tasks: ['jshint']

            sass:
                files: [
                  '<%= dirs.root %>/sass-hidden/**/*.sass'
                  '<%= dirs.root %>/sass-hidden/**/*.scss'
                ]
                tasks: ['sass']

        sass:
            options:
                style: 'expanded'
                includePaths: [
                  'bower_components/reveal.js/css'
                ]
            all:
                files: [
                    expand: true
                    cwd: '<%= dirs.root %>/sass-hidden'
                    src: [
                      '**/*.sass'
                      '**/*.scss'
                    ]
                    dest: '<%= dirs.root %>/stylesheets'
                    ext: '.css'
                ]

        connect:
            livereload:
                options:
                    port: 9000
                    # Change hostname to '0.0.0.0' to access
                    # the server from outside.
                    hostname: 'localhost'
                    base: '.'
                    open: true
                    livereload: true

        coffeelint:
            options:
                indentation:
                    value: 4
                max_line_length:
                    level: 'ignore'

            all: [
              'Gruntfile.coffee'
            ]

        jshint:
            options:
                jshintrc: '.jshintrc'

            all: [
              '<%= dirs.root %>/js/*.js'
            ]

        copy:
            webapp:
                files: [{
                    expand: true
                    src: 'bower_components/**'
                    dest: '<%= dirs.webapp %>'
                },{
                    expand: true
                    cwd: '<%= dirs.root %>'
                    src: '**/*.*'
                    dest: '<%= dirs.webapp %>'
                }]
            dist:
                files: [{
                    expand: true
                    src: [
                        '<%= dirs.root %>/slides/**'
                        'bower_components/**'
                        '<%= dirs.root %>/js/**'
                        '<%= dirs.root %>/stylesheets/*.css'
                        '<%= dirs.root %>/resources/**'
                    ]
                    dest: '<%= dirs.dist %>'
                },{
                    expand: true
                    src: [
                      '<%= dirs.root %>/index.html'
                    ]
                    dest: '<%= dirs.dist %>'
                    filter: 'isFile'
                }]




    # Load all grunt tasks.
    require('load-grunt-tasks')(grunt)

    grunt.registerTask 'buildIndex',
        'Build index.html from templates/_index.html and slides/list.json.',
        ->
            indexTemplate = grunt.file.read(grunt.template.process '<%= dirs.root %>/templates/_index.html')
            sectionTemplate = grunt.file.read(grunt.template.process '<%= dirs.root %>/templates/_section.html')
            slides = grunt.file.readJSON(grunt.template.process '<%= dirs.root %>/slides/list.json')

            html = grunt.template.process indexTemplate, data:
                slides:
                    slides
                section: (slide) ->
                    grunt.template.process sectionTemplate, data:
                        slide:
                            slide
            grunt.file.write grunt.template.process('<%= dirs.root %>/deck.html'), html

    grunt.registerTask 'lint',
        '*Lint* javascript and coffee files.', [
            'coffeelint'
            'jshint'
        ]

    grunt.registerTask 'dist',
        'Save presentation files to *dist* directory.', [
            'lint'
            'sass'
            'buildIndex'
            'copy'
        ]

    grunt.registerTask 'sbt',
        'Save presentation files to *dist* directory.', [
            'lint'
            'sass'
            'buildIndex'
            'copy:webapp'
        ]

    # Define default task.
    grunt.registerTask 'default', [
        'sbt'
        'watch'
    ]
