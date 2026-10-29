package com.rabbyx.newtonixairevivededition.interfaces

class HTML {

    val welcomeHtmlPhysics = """
         <html>
    <head>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 8px;
            background: linear-gradient(45deg, #ffffff, #e0e0e0, #b0c4de, #f0f8ff); /* Light mode animated gradient */
            background-size: 200% 200%;
            animation: backgroundShift 10s ease infinite;
            transition: background 0.5s ease;
            overflow-x: hidden;
        }
        @media (prefers-color-scheme: dark) {
            body {
                background: linear-gradient(45deg, #2f4f4f, #4a4a4a, #708090, #2f4f4f); /* Dark mode animated gradient */
            }
            p {
                color: #ddd; /* Dark mode text color */
            }
        }
        @keyframes backgroundShift {
            0% { background-position: 0% 50%; }
            50% { background-position: 100% 50%; }
            100% { background-position: 0% 50%; }
        }
        .gradient-text {
            font-size: 30px;
            font-weight: bold;
            background: linear-gradient(45deg, #4285f4, #34a853, #fbbc05, #ea4335);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent;
            animation: gradientShift 5s ease infinite;
            display: inline-block;
            background-size: 200% 200%;
        }
        @keyframes gradientShift {
            0% { background-position: 0% 0%; }
            25% { background-position: 100% 0%; }
            50% { background-position: 100% 100%; }
            75% { background-position: 0% 100%; }
            100% { background-position: 0% 0%; }
        }
        p {
            font-size: 16px;
            color: #333;
        }
        #heartCanvas {
            width: 100vw;
            height: 100vh;
            position: fixed;
            top: 0;
            left: 0;
            z-index: -1; /* Place canvas behind content */
        }
        .content {
            position: relative;
            z-index: 1; /* Ensure content is above canvas */
            text-align: center;
            padding: 20px;
        }
    </style>
</head>
<body>
    <canvas id="heartCanvas"></canvas>
    <div class="content">
        <h2 class="gradient-text">Welcome to <br/>NewtoniX AI<sub><small>Revived</small></sub><pre>Powered by RABBYX</pre></h2>
        <h1 class="gradient-text">Mathematics <b>/</b> <br/> Mathematical Physics/<br/>Mathematical Chemistry</h1><br/>
        <p><font color="#F44336">Feel free to ask questions, upload images, or explore the features!</font></p>
        </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/three@0.128.0/examples/js/loaders/GLTFLoader.js"></script>
    <script>
        // Three.js setup for 3D heart model
        const canvas = document.getElementById('heartCanvas');
        const scene = new THREE.Scene();
        const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
        const renderer = new THREE.WebGLRenderer({ canvas: canvas, alpha: true });
        renderer.setSize(window.innerWidth, window.innerHeight);

        // Adjust canvas size on window resize
        window.addEventListener('resize', () => {
            camera.aspect = window.innerWidth / window.innerHeight;
            camera.updateProjectionMatrix();
            renderer.setSize(window.innerWidth, window.innerHeight);
        });

        // Lighting
        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
        scene.add(ambientLight);
        const pointLight = new THREE.PointLight(0xffffff, 0.5);
        pointLight.position.set(5, 5, 5);
        scene.add(pointLight);

        // Load heart model (using a placeholder URL for a heart GLTF model)
        const loader = new THREE.GLTFLoader();
        loader.load(
            './bio/human_heart.glb', // Replace with actual heart model URL
            (gltf) => {
                const heart = gltf.scene;
                heart.scale.set(3.0, 3.0, 3.0); // Larger scale for full-screen background
                scene.add(heart);

                // Animation for spinning
                function animate() {
                    requestAnimationFrame(animate);
                    heart.rotation.y += 0.01; // Spin around Y-axis
                    renderer.render(scene, camera);
                }
                animate();
            },
            (xhr) => {
                console.log((xhr.loaded / xhr.total * 100) + '% loaded');
            },
            (error) => {
                console.error('Error loading model:', error);
            }
        );

        camera.position.z = 10; // Adjusted for larger model and full-screen view
    </script>
</body>
</html>
        """.trimIndent()

    val welcomeHtmlChemistry = """
        <html>
        <head>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    margin: 0;
                    padding: 8px;
                    background-color: transparent;
                }
                .gradient-text {
                    font-size: 30px;
                    font-weight: bold;
                    background: linear-gradient(45deg, #4285f4, #34a853, #fbbc05, #ea4335);
                    -webkit-background-clip: text;
                    background-clip: text;
                    color: transparent;
                    animation: gradientShift 5s ease infinite;
                    display: inline-block;
                    background-size: 200% 200%;
                }
                @keyframes gradientShift {
                    0% { background-position: 0% 0%; }
                    25% { background-position: 100% 0%; }
                    50% { background-position: 100% 100%; }
                    75% { background-position: 0% 100%; }
                    100% { background-position: 0% 0%; }
                }
                p {
                    font-size: 16px;
                    color: #333;
                }
            </style>
        </head>
        <body>
            <h2 class="gradient-text">Welcome to <br/>NewtoniX AI<sub><small>Revived</small></sub><pre>Powered by RABBYX</pre></h2>
            <h1 class="gradient-text">Chemistry <b>/</b> <br/> Reactions/<br/>Compounds</h1><br/>
            <p> <font color="#F44336">Feel free to ask questions, upload images, or explore the features! </font> </p>
            <p> <font color="#F44336">আপনি নির্দ্বিধায় প্রশ্ন জিজ্ঞাসা করতে পারেন, ছবি আপলোড করতে পারেন, বা অন্বেষণ করতে পারেন! </font> </p>
        </body>
        </html>
        """.trimIndent()



    val welcomeHtmlBiology = """
        <html>
    <head>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 8px;
            background: linear-gradient(45deg, #ffffff, #e0e0e0, #b0c4de, #f0f8ff); /* Light mode animated gradient */
            background-size: 200% 200%;
            animation: backgroundShift 10s ease infinite;
            transition: background 0.5s ease;
            overflow-x: hidden;
        }
        @media (prefers-color-scheme: dark) {
            body {
                background: linear-gradient(45deg, #2f4f4f, #4a4a4a, #708090, #2f4f4f); /* Dark mode animated gradient */
            }
            p {
                color: #ddd; /* Dark mode text color */
            }
        }
        @keyframes backgroundShift {
            0% { background-position: 0% 50%; }
            50% { background-position: 100% 50%; }
            100% { background-position: 0% 50%; }
        }
        .gradient-text {
            font-size: 30px;
            font-weight: bold;
            background: linear-gradient(45deg, #4285f4, #34a853, #fbbc05, #ea4335);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent;
            animation: gradientShift 5s ease infinite;
            display: inline-block;
            background-size: 200% 200%;
        }
        @keyframes gradientShift {
            0% { background-position: 0% 0%; }
            25% { background-position: 100% 0%; }
            50% { background-position: 100% 100%; }
            75% { background-position: 0% 100%; }
            100% { background-position: 0% 0%; }
        }
        p {
            font-size: 16px;
            color: #333;
        }
        #heartCanvas {
            width: 100vw;
            height: 100vh;
            position: fixed;
            top: 0;
            left: 0;
            z-index: -1; /* Place canvas behind content */
        }
        .content {
            position: relative;
            z-index: 1; /* Ensure content is above canvas */
            text-align: center;
            padding: 20px;
        }
    </style>
</head>
<body>
    <canvas id="heartCanvas"></canvas>
    <div class="content">
        <h2 class="gradient-text">Welcome to <br/>NewtoniX AI<sub><small>Revived</small></sub><pre>Powered by RABBYX</pre></h2>
        <h1 class="gradient-text">Biology <b>/</b> <br/> Zoology/<br/>Anatomy</h1><br/>
        <p><font color="#F44336">Feel free to ask questions, upload images, or explore the features!</font></p>
        
        <p><font color="#F44336">আপনি শুধুমাত্র অরগানিজমের ইংরেজিতে নামটি লিখুন, ছবি আপলোড করতে পারেন, বা একই সাথে ছবি আপলোড ও নাম লিখতে পারেন!</font></p>
    </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/three@0.128.0/examples/js/loaders/GLTFLoader.js"></script>
    <script>
        // Three.js setup for 3D heart model
        const canvas = document.getElementById('heartCanvas');
        const scene = new THREE.Scene();
        const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
        const renderer = new THREE.WebGLRenderer({ canvas: canvas, alpha: true });
        renderer.setSize(window.innerWidth, window.innerHeight);

        // Adjust canvas size on window resize
        window.addEventListener('resize', () => {
            camera.aspect = window.innerWidth / window.innerHeight;
            camera.updateProjectionMatrix();
            renderer.setSize(window.innerWidth, window.innerHeight);
        });

        // Lighting
        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
        scene.add(ambientLight);
        const pointLight = new THREE.PointLight(0xffffff, 0.5);
        pointLight.position.set(5, 5, 5);
        scene.add(pointLight);

        // Load heart model (using a placeholder URL for a heart GLTF model)
        const loader = new THREE.GLTFLoader();
        loader.load(
            './bio/human_heart.glb', // Replace with actual heart model URL
            (gltf) => {
                const heart = gltf.scene;
                heart.scale.set(3.0, 3.0, 3.0); // Larger scale for full-screen background
                scene.add(heart);

                // Animation for spinning
                function animate() {
                    requestAnimationFrame(animate);
                    heart.rotation.y += 0.01; // Spin around Y-axis
                    renderer.render(scene, camera);
                }
                animate();
            },
            (xhr) => {
                console.log((xhr.loaded / xhr.total * 100) + '% loaded');
            },
            (error) => {
                console.error('Error loading model:', error);
            }
        );

        camera.position.z = 10; // Adjusted for larger model and full-screen view
    </script>
</body>
</html>
        """.trimIndent()
// https://sketchfab-prod-media.s3.amazonaws.com/archives/3f8072336ce94d18b3d0d055a1ece089/glb/c557d224ed7d44c19b95c440c80d5f3a/realistic_human_heart.glb?AWSAccessKeyId=ASIAZ4EAQ242FTP7ARED&Signature=ce6btf9v2Ne%2FCQxNWNOQz4yboig%3D&x-amz-security-token=IQoJb3JpZ2luX2VjEBEaCWV1LXdlc3QtMSJGMEQCIEtsO%2BN0YRIeGMmiUqoGtHpRIfTgafYy%2Bwlz6%2BFHbm3CAiALz%2FT%2FfCBnSxhM%2Bp0ZPHEcBsW0Png5FS39Mm4vSzmrlyq7BQiq%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BEAAaDDY3ODg3NDM3MTg5MiIM%2BEEgslcIRKyzTKpZKo8FfwGvI%2BD5WW5c32ppypVLBmRStDjAc6ZGUQSMgYdCB1Awc%2B8BnEQlOxpmDFZU6IjFShT%2BfbhyRo5Gu5wFSgC1FyX9I3NzRGaSYRseWOsDouwA2aNOq0a5jUn%2FDcwnvbpu45hGkfZj8MZ3FrjZRH4s8NkgC1upuJw%2Fv1102CzaXMCEpBKr%2FPu5%2Fx9LZYYVin7vL4ySecg8ZMAxJppKnjVkzYN13otujwOt6y4%2BM2gckmSTJUR9kQ6NoLCzy8Z%2BvB2V5GTSNb%2Ba9yjUelZOyf1J6P%2FEK85%2FJCGQoNYaRcS664ZfufacoIc%2BtOCsGAKghtr2SkXDCyo%2B8tU2AYsn5EKVnJXA3nJVSwbiABGmx7FlVrwVQycElEV1jO83yopTA88QRdy4mz5DFlymg9En%2BK%2B0yJBn0se9RtuCSJMibgs5%2B8aX0J%2FskBjsXvO%2B2qMwAmft2hQOwFIkgFXWEP0ni1qZYTEZx8dX9UaXkKvFCTIhMvePCzOXkHnv9gejQXoKk0VLIz6tMAXCpMyukmsuWlPBSeYeknUNkDQY12t9cn7jOPqKh9h8ThKoOr4iXdZ%2BV5ulHeiIaJHtVNLJer4Q9qmXRjliJ8D7feVYE3F0f%2FUD3pflPHpeWRPFesynXTC7%2Br2U5LzdyaJ5F9lxXIkLnfJGGAiGb3kaz5lLtRl66kNdbP3Tttjd%2FoWrXJgKWB5OsAp7%2FGCbwM6f7HkSRN2rpHNB0UM%2FwflCYGTOpsvEpNFVSm%2Fc2hfJTxQu03T5uAPtXDY%2BN0vO25Dn3k0uDldYCiImsBBUjGtCQDGiAtT%2BfHo6fIRSvp0gBB4I3jUglmz4EDI28%2B9Yi4tRMePGzqcsd782VJ%2FmDJOOZ2mLgqMgsqayBTCAs8nABjqyAcllzg3wgz%2BLCnzNBsNyXo3VWcBWxgKzr1k1F7tTbOnk4E1wwQhOMAKNTYNesSnHJv8JRw5NCi9YsDrbjj0uUxV%2Bnj371VWmeQ1hEPAk7IYCCBYH3YuBrR6YZiYYNqSUrv%2F97lzcd7Em2uxIgek21LmpPsfgrHp7JIQyL0kiHCKHpjqegOFP7H2xwoSgrhgFaEiR2IwbPa7PamTmctSvWmp4aLjPuJTliAcO3mqehl8podo%3D&Expires=1746036569


    fun latexViewerMarkdown(text: String): String {
        val htmlContent = """
            <html>
                <head>
                    <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
                    <style>
                        body { 
                            font-family: Arial, sans-serif; 
                            font-size: 16px; 
                            margin: 0; 
                            padding: 8px; 
                            width: 100%;
                            background: #f5f5f5;
                            color: #333333;
                        }
                        #markdown {
                            animation: fadeInFromRight 1s ease-out forwards;
                            background: #ffffff;
                            color: inherit;
                            padding: 20px;
                            width: 100%;
                            border-radius: 8px;
                            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
                            box-sizing: border-box;
                        }
                         @media (prefers-color-scheme: dark) {
                           body {
                                background: #121212;
                                color: #ffffff;
                            }
                            #markdown {
                                background: #1a1a1a;
                                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
                            }
                        }
                        @keyframes fadeInFromRight {
                            0% {
                                opacity: 0;
                                transform: translateX(100px);
                            }
                            100% {
                                opacity: 1;
                                transform: translateX(0);
                            }
                        }
                    </style>
                </head>
                <body>
                    <div id="markdown"></div>
                    <script>
                        document.getElementById('markdown').innerHTML = marked.parse(`$text`);
                    </script>
                </body>
                </html>
            """.trimIndent()
        return htmlContent
    }

    fun molViewer(text: String): String {
        val htmlContent = """
        <html>
        <head>
          <meta http-equiv="X-UA-Compatible" content="chrome=1">
          <link rel="stylesheet" href="file:///android_asset/chem/install/ChemDoodleWeb.css" type="text/css">
          <link rel="stylesheet" href="file:///android_asset/chem/install/uis/jquery-ui-1.11.4.css" type="text/css">
          <script type="text/javascript" src="file:///android_asset/chem/install/ChemDoodleWeb.js"></script>
          <script type="text/javascript" src="file:///android_asset/chem/install/uis/ChemDoodleWeb-uis.js"></script>
          <title>ChemDoodle Web Component Sketcher</title>
        </head>
        <body>
        <center>
          <!-- Sketcher Canvas -->
          <canvas id="sketcher" width="500" height="300"></canvas>

          <script>
            // Customizing colors
            ChemDoodle.ELEMENT['H'].jmolColor = 'black';
            ChemDoodle.ELEMENT['S'].jmolColor = '#B9A130';

            // Initialize Sketcher
            var sketcher = new ChemDoodle.SketcherCanvas('sketcher', 500, 300, { useServices: true });
            sketcher.specs.atoms_displayTerminalCarbonLabels_2D = true;
            sketcher.specs.atoms_useJMOLColors = true;
            sketcher.specs.bonds_clearOverlaps_2D = true;
            sketcher.specs.shapes_color = '#c10000';

            // Load initial molecule from parameter
            try {
              const initialMol = "${text.replace("\n", "\\n").replace("\"", "\\\"")}";
              if (initialMol.trim()) {
                const molecule = ChemDoodle.readMOL(initialMol);
                sketcher.loadMolecule(molecule);
              }
            } catch (e) {
              console.error('Error loading initial MOL data:', e);
            }

            // Store the last known MOL data to detect changes
            let lastMolData = '';

            // Export MOL data to Android if changed
            function exportMOL() {
              const mol = ChemDoodle.writeMOL(sketcher.getMolecule());
              // Only proceed if the molecule has changed
              if (mol !== lastMolData) {
                lastMolData = mol;
                if (window.AndroidBridge) {
                  try {
                    window.AndroidBridge.sendMOLData(mol);
                    console.log('MOL data sent to Android');
                  } catch (e) {
                    console.error('Error sending MOL data to Android:', e);
                  }
                } else {
                  console.warn('AndroidBridge not available');
                }
              }
            }

            // Check for updates every 500ms
            setInterval(exportMOL, 500);

            // Initial export
            exportMOL();
          </script>
        </center>
        </body>
        </html>
    """.trimIndent()

        return htmlContent
    }


    fun latexViewer(text: String): String {
        val htmlContent = """
            <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <script src="https://polyfill.io/v3/polyfill.min.js?features=es6"></script>
                    <script src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>
                    <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
                    <style>
                   </head>
                         body { font-size: 18px; margin: 0; padding: 8px; }
                        .mjx-chtml { font-size: 120%; }
                        
                        @media (prefers-color-scheme: dark) {
                            body {
                                background: #121212;
                                color: #ffffff;
                            }
                            
                        }
                        
                    </style>
                
                <body>
                    <div id="math">
                        $$$text$$$
                    </div>
                    <script>
                        MathJax.typeset();
                    </script>
                </body>
                </html>
            """.trimIndent()
        return htmlContent
    }




}