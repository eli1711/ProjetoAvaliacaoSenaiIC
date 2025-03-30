<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Avaliação de Satisfação</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f5f5f5;
            margin: 0;
            padding: 20px;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
            background-color: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }
        h1 {
            color: #333;
            text-align: center;
            margin-bottom: 30px;
        }
        .question {
            margin-bottom: 20px;
            padding: 15px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        .question h3 {
            margin-top: 0;
            color: #444;
        }
        .rating {
            display: flex;
            justify-content: space-between;
            margin-top: 10px;
        }
        .rating-option {
            text-align: center;
        }
        .btn-submit {
            background-color: #4CAF50;
            color: white;
            padding: 12px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            margin-top: 20px;
            width: 100%;
        }
        .btn-submit:hover {
            background-color: #45a049;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Avaliação de Satisfação</h1>
        
        <form action="salvarAvaliacao" method="post">
            <div class="question">
                <h3>1. Como você avalia a qualidade do ensino?</h3>
                <div class="rating">
                    <div class="rating-option">
                        <input type="radio" id="q1-1" name="q1" value="1" required>
                        <label for="q1-1">1 (Péssimo)</label>
                    </div>
                    <div class="rating-option">
                        <input type="radio" id="q1-2" name="q1" value="2">
                        <label for="q1-2">2</label>
                    </div>
                    <div class="rating-option">
                        <input type="radio" id="q1-3" name="q1" value="3">
                        <label for="q1-3">3</label>
                    </div>
                    <div class="rating-option">
                        <input type="radio" id="q1-4" name="q1" value="4">
                        <label for="q1-4">4</label>
                    </div>
                    <div class="rating-option">
                        <input type="radio" id="q1-5" name="q1" value="5">
                        <label for="q1-5">5 (Excelente)</label>
                    </div>
                </div>
            </div>
            
            <div class="question">
                <h3>2. Como você avalia a infraestrutura da escola?</h3>
                <div class="rating">
                    <div class="rating-option">
                        <input type="radio" id="q2-1" name="q2" value="1" required>
                        <label for="q2-1">1 (Péssimo)</label>
                    </div>
                    <div class="rating-option">
                        <input type="radio" id="q2-2" name="q2" value="2">
                        <label for="q2-2">2</label>
                    </div>
                    <div class="rating-option">
                        <input type="radio" id="q2-3" name="q2" value="3">
                        <label for="q2-3">3</label>
                    </div>
                    <div class="rating-option">
                        <input type="radio" id="q2-4" name="q2" value="4">
                        <label for="q2-4">4</label>
                    </div>
                    <div class="rating-option">
                        <input type="radio" id="q2-5" name="q2" value="5">
                        <label for="q2-5">5 (Excelente)</label>
                    </div>
                </div>
            </div>
            
            <div class="question">
                <h3>3. Comentários adicionais:</h3>
                <textarea name="comentario" rows="4" style="width: 100%; padding: 10px;"></textarea>
            </div>
            
            <button type="submit" class="btn-submit">Enviar Avaliação</button>
        </form>
    </div>
</body>
</html>