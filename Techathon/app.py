from flask import Flask, request, render_template, send_from_directory

app = Flask(__name__)

# Serve static files from the 'uploads' directory
@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory('uploads', filename)

@app.route('/')
def index():
    # Load the content of text file
    text_content = ""
    try:
        with open('uploads/text_content.txt', 'r') as file:
            text_content = file.read()
    except FileNotFoundError:
        text_content = "No text content available"

    # Provide the path for the image file
    image_url = '/uploads/image.png'

    return render_template('index.html', text_content=text_content, image_url=image_url)

@app.route('/upload', methods=['POST'])
def upload_files():
    if 'image_file' not in request.files or 'text_content' not in request.form:
        return 'No file or text content provided', 400

    image_file = request.files['image_file']
    text_content = request.form['text_content']

    # Save image file
    image_path = 'uploads/image.png'
    image_file.save(image_path)

    # Save text content
    text_path = 'uploads/text_content.txt'
    with open(text_path, 'w') as file:
        file.write(text_content)

    return 'Files successfully uploaded', 200

if __name__ == '__main__':
    app.run(port=5000, debug=True)
