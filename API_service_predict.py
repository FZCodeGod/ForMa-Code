from flask import Flask, request, jsonify
from tensorflow.keras.models import load_model
import numpy as np

# 初始化 Flask 应用
app = Flask(__name__)

# 加载模型（请根据实际模型路径修改）
model_path = 'D:/Forma_model/lstm model(60to60).h5'
model = load_model(model_path)
print("模型已成功加载！")

# 定义预测的路由
@app.route('/predict', methods=['POST'])
def predict():
    try:
        # 从请求中获取输入数据
        data = request.get_json()  # JSON 格式输入
        if not data or 'input' not in data:
            return jsonify({'error': '无效的输入数据，请提供 "input" 字段'}), 400
        
        # 将输入数据转换为 numpy 数组
        input_data = np.array(data['input']).reshape(-1, 60, 8)  # 假设输入为一维数组
        print(f"收到的输入数据: {input_data}")
        
        # 使用模型进行预测
        prediction = model.predict(input_data)
        print(f"预测结果: {prediction}")
        
        # 返回预测结果
        return jsonify({'prediction': prediction.tolist()}), 200
    
    except Exception as e:
        print(f"预测时发生错误: {e}")
        return jsonify({'error': str(e)}), 500

# 定义根路由
@app.route('/')
def home():
    return "LSTM 模型 API 服务运行中！"

# 启动 Flask 服务
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
